package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Appointment
import com.example.data.model.ProviderBusiness
import com.example.data.model.QueueTicket
import com.example.data.model.Review
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class GroomlyRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. USER PROFILE ---
    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun ensureOrUpdateUserProfile(
        displayName: String,
        genderPreference: String = "UNISEX",
        cityLocation: String = "Indiranagar, Bengaluru",
        savedAddresses: List<String> = listOf("Home • 12th Main, Indiranagar, Bengaluru"),
        favouriteProviderIds: List<String> = listOf("prov_royal_gents", "prov_velvet_bridal"),
        walletBalanceInr: Long = 1500L,
        loyaltyPoints: Long = 340L,
        whatsappAlertsEnabled: Boolean = true,
        pushNotificationsEnabled: Boolean = true,
        marketingConsent: Boolean = false
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName.take(80).ifBlank { "Groomly Member" },
                    "genderPreference" to genderPreference,
                    "cityLocation" to cityLocation.take(120),
                    "savedAddresses" to savedAddresses.take(10),
                    "favouriteProviderIds" to favouriteProviderIds.take(10),
                    "walletBalanceInr" to walletBalanceInr,
                    "loyaltyPoints" to loyaltyPoints,
                    "whatsappAlertsEnabled" to whatsappAlertsEnabled,
                    "pushNotificationsEnabled" to pushNotificationsEnabled,
                    "marketingConsent" to marketingConsent,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updatePayload = mapOf(
                    "displayName" to displayName.take(80).ifBlank { "Groomly Member" },
                    "genderPreference" to genderPreference,
                    "cityLocation" to cityLocation.take(120),
                    "savedAddresses" to savedAddresses.take(10),
                    "favouriteProviderIds" to favouriteProviderIds.take(10),
                    "walletBalanceInr" to walletBalanceInr,
                    "loyaltyPoints" to loyaltyPoints,
                    "whatsappAlertsEnabled" to whatsappAlertsEnabled,
                    "pushNotificationsEnabled" to pushNotificationsEnabled,
                    "marketingConsent" to marketingConsent,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- 2. SMART APPOINTMENTS ---
    fun observeAppointments(): Flow<List<Appointment>> = flow {
        val uid = requireUserId()
        val path = "appointments"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Appointment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getUserAppointments(): Result<List<Appointment>> = runCatching {
        val uid = requireUserId()
        val path = "appointments"
        try {
            val snap = db.collection(path).whereEqualTo("userId", uid).get().await()
            snap.toObjects(Appointment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun getAppointmentById(appointmentId: String): Result<Appointment?> = runCatching {
        val docRef = db.collection("appointments").document(appointmentId)
        try {
            val snap = docRef.get().await()
            snap.toObject(Appointment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createAppointment(
        providerId: String,
        providerName: String,
        vertical: String,
        serviceSummary: String,
        staffId: String,
        staffName: String,
        dateIso: String,
        timeSlot: String,
        durationMinutes: Int,
        totalAmountInr: Int,
        advancePaidInr: Int,
        remainingBalanceInr: Int,
        paymentMethod: String,
        paymentType: String,
        couponCode: String = "NONE",
        isHomeService: Boolean = false
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("appointments")
        val docRef = colRef.document()
        val payload = mapOf(
            "userId" to uid,
            "providerId" to providerId.take(80),
            "providerName" to providerName.take(100),
            "vertical" to vertical,
            "serviceSummary" to serviceSummary.take(240),
            "staffId" to staffId.take(80),
            "staffName" to staffName.take(80),
            "dateIso" to dateIso.take(20),
            "timeSlot" to timeSlot.take(30),
            "durationMinutes" to durationMinutes.coerceIn(10, 600).toLong(),
            "totalAmountInr" to totalAmountInr.coerceIn(0, 500000).toLong(),
            "advancePaidInr" to advancePaidInr.coerceIn(0, totalAmountInr).toLong(),
            "remainingBalanceInr" to remainingBalanceInr.coerceIn(0, totalAmountInr).toLong(),
            "paymentMethod" to paymentMethod,
            "paymentType" to paymentType,
            "couponCode" to couponCode.ifBlank { "NONE" }.take(40),
            "isHomeService" to isHomeService,
            "status" to "CONFIRMED",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun rescheduleAppointment(
        appointmentId: String,
        newDateIso: String,
        newTimeSlot: String,
        newStaffId: String,
        newStaffName: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("appointments").document(appointmentId)
        val updates = mapOf(
            "dateIso" to newDateIso.take(20),
            "timeSlot" to newTimeSlot.take(30),
            "staffId" to newStaffId.take(80),
            "staffName" to newStaffName.take(80),
            "status" to "RESCHEDULED",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: String,
        advancePaidInr: Long,
        remainingBalanceInr: Long
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("appointments").document(appointmentId)
        val updates = mapOf(
            "status" to newStatus,
            "advancePaidInr" to advancePaidInr,
            "remainingBalanceInr" to remainingBalanceInr,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 3. LIVE REAL-TIME QUEUE ---
    fun observeQueueTickets(): Flow<List<QueueTicket>> = flow {
        val uid = requireUserId()
        val path = "queue_tickets"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(QueueTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun joinLiveQueue(
        providerId: String,
        providerName: String,
        serviceName: String,
        staffName: String,
        queueNumber: Int,
        currentlyServingNumber: Int,
        estimatedWaitMinutes: Int
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("queue_tickets").document()
        val qrCode = "GRM-Q-${queueNumber}-${providerId.takeLast(4).uppercase()}"
        val payload = mapOf(
            "userId" to uid,
            "providerId" to providerId.take(80),
            "providerName" to providerName.take(100),
            "serviceName" to serviceName.take(120),
            "staffName" to staffName.take(80),
            "queueNumber" to queueNumber.coerceIn(1, 999).toLong(),
            "currentlyServingNumber" to currentlyServingNumber.coerceIn(1, 999).toLong(),
            "estimatedWaitMinutes" to estimatedWaitMinutes.coerceIn(0, 300).toLong(),
            "qrCheckInCode" to qrCode,
            "status" to "WAITING",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateQueueTicketState(
        ticketId: String,
        currentlyServingNumber: Int,
        estimatedWaitMinutes: Int,
        newStatus: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("queue_tickets").document(ticketId)
        val updates = mapOf(
            "currentlyServingNumber" to currentlyServingNumber.coerceIn(1, 999).toLong(),
            "estimatedWaitMinutes" to estimatedWaitMinutes.coerceIn(0, 300).toLong(),
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 4. PROVIDER BUSINESSES ---
    fun observeProviderBusinesses(): Flow<List<ProviderBusiness>> = flow {
        val uid = requireUserId()
        val path = "provider_businesses"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ProviderBusiness::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun registerProviderBusiness(
        ownerName: String,
        businessName: String,
        category: String,
        vertical: String,
        genderFocus: String,
        cityArea: String,
        openingHours: String,
        homeServiceEnabled: Boolean,
        autoAcceptBookings: Boolean,
        commissionRatePercent: Int = 15,
        subscriptionTier: String = "GROWTH"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("provider_businesses").document()
        val payload = mapOf(
            "userId" to uid,
            "ownerName" to ownerName.take(80),
            "businessName" to businessName.take(100),
            "category" to category.take(80),
            "vertical" to vertical,
            "genderFocus" to genderFocus,
            "cityArea" to cityArea.take(120),
            "openingHours" to openingHours.take(80),
            "homeServiceEnabled" to homeServiceEnabled,
            "autoAcceptBookings" to autoAcceptBookings,
            "verificationStatus" to "PENDING_REVIEW",
            "commissionRatePercent" to commissionRatePercent.coerceIn(5, 40).toLong(),
            "subscriptionTier" to subscriptionTier,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateProviderGovernance(
        business: ProviderBusiness,
        verificationStatus: String,
        commissionRatePercent: Int,
        subscriptionTier: String,
        autoAcceptBookings: Boolean,
        homeServiceEnabled: Boolean
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("provider_businesses").document(business.id)
        val updates = mapOf(
            "ownerName" to business.ownerName,
            "businessName" to business.businessName,
            "category" to business.category,
            "genderFocus" to business.genderFocus,
            "cityArea" to business.cityArea,
            "openingHours" to business.openingHours,
            "homeServiceEnabled" to homeServiceEnabled,
            "autoAcceptBookings" to autoAcceptBookings,
            "verificationStatus" to verificationStatus,
            "commissionRatePercent" to commissionRatePercent.coerceIn(5, 40).toLong(),
            "subscriptionTier" to subscriptionTier,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 5. VERIFIED REVIEWS ---
    fun observeReviews(): Flow<List<Review>> = flow {
        val uid = requireUserId()
        val path = "reviews"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Review::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: com.google.firebase.Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun submitReview(
        appointmentId: String,
        providerId: String,
        providerName: String,
        staffName: String,
        rating: Int,
        staffRating: Int,
        comment: String,
        photoTag: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("reviews").document()
        val payload = mapOf(
            "userId" to uid,
            "appointmentId" to appointmentId.take(80),
            "providerId" to providerId.take(80),
            "providerName" to providerName.take(100),
            "staffName" to staffName.take(80),
            "rating" to rating.coerceIn(1, 5).toLong(),
            "staffRating" to staffRating.coerceIn(1, 5).toLong(),
            "comment" to comment.take(500).ifBlank { "Great service and hygiene!" },
            "photoTag" to photoTag.take(120),
            "isReported" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleReportReview(review: Review): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("reviews").document(review.id)
        val updates = mapOf(
            "rating" to review.rating,
            "staffRating" to review.staffRating,
            "comment" to review.comment,
            "photoTag" to review.photoTag,
            "isReported" to !review.isReported,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }
}
