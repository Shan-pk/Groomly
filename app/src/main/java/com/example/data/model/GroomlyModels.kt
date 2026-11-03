package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val genderPreference: String = "UNISEX",
    val cityLocation: String = "Indiranagar, Bengaluru",
    val savedAddresses: List<String> = listOf("Home • 12th Main, Indiranagar, Bengaluru"),
    val favouriteProviderIds: List<String> = listOf("prov_royal_gents", "prov_velvet_bridal"),
    val walletBalanceInr: Long = 1500L,
    val loyaltyPoints: Long = 340L,
    val whatsappAlertsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val marketingConsent: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Appointment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val vertical: String = "SALON",
    val serviceSummary: String = "",
    val staffId: String = "",
    val staffName: String = "",
    val dateIso: String = "",
    val timeSlot: String = "",
    val durationMinutes: Long = 45L,
    val totalAmountInr: Long = 500L,
    val advancePaidInr: Long = 100L,
    val remainingBalanceInr: Long = 400L,
    val paymentMethod: String = "UPI",
    val paymentType: String = "ADVANCE",
    val couponCode: String = "NONE",
    val isHomeService: Boolean = false,
    val status: String = "CONFIRMED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class QueueTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val serviceName: String = "",
    val staffName: String = "",
    val queueNumber: Long = 12L,
    val currentlyServingNumber: Long = 10L,
    val estimatedWaitMinutes: Long = 18L,
    val qrCheckInCode: String = "GRM-Q-101",
    val status: String = "WAITING",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProviderBusiness(
    @DocumentId val id: String = "",
    val userId: String = "",
    val ownerName: String = "",
    val businessName: String = "",
    val category: String = "",
    val vertical: String = "SALON",
    val genderFocus: String = "UNISEX",
    val cityArea: String = "",
    val openingHours: String = "09:00 AM - 09:30 PM",
    val homeServiceEnabled: Boolean = true,
    val autoAcceptBookings: Boolean = true,
    val verificationStatus: String = "VERIFIED",
    val commissionRatePercent: Long = 15L,
    val subscriptionTier: String = "GROWTH",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Review(
    @DocumentId val id: String = "",
    val userId: String = "",
    val appointmentId: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val staffName: String = "",
    val rating: Long = 5L,
    val staffRating: Long = 5L,
    val comment: String = "",
    val photoTag: String = "Fresh Look",
    val isReported: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class AppRoleMode(val label: String, val badge: String) {
    CUSTOMER("Customer App", "Book & Queue"),
    PROVIDER("Provider Studio", "Salon & Staff SaaS"),
    ADMIN("Super Admin", "Marketplace Control"),
    ARCHITECTURE("System Blueprint", "SaaS Architecture")
}

data class ServiceCategoryItem(
    val id: String,
    val name: String,
    val vertical: String,
    val genderTag: String,
    val startingPriceInr: Int
)

data class StaffMemberProfile(
    val id: String,
    val name: String,
    val roleTitle: String,
    val specialty: String,
    val rating: Double,
    val completedJobs: Int,
    val experienceYears: Int,
    val priceAddonInr: Int,
    val shiftHours: String,
    val breakWindow: String,
    val isAvailableToday: Boolean = true,
    val portfolioHighlights: List<String> = emptyList()
)

data class MarketplaceServiceItem(
    val id: String,
    val name: String,
    val categoryName: String,
    val description: String,
    val durationMinutes: Int,
    val bufferMinutes: Int,
    val priceInr: Int,
    val discountPriceInr: Int,
    val genderEligibility: String,
    val homeServiceAvailable: Boolean,
    val isBundle: Boolean = false,
    val bundleIncludedItems: List<String> = emptyList()
)

data class MarketplaceProvider(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val vertical: String, // SALON, BRIDAL, DIETITIAN, DERMATOLOGY
    val genderFocus: String, // MENS, WOMENS, UNISEX
    val address: String,
    val distanceKm: Double,
    val latitude: Double = 12.9716,
    val longitude: Double = 77.6412,
    val rating: Double,
    val reviewCount: Int,
    val openingHours: String,
    val contactPhone: String,
    val isVerified: Boolean,
    val isOpenNow: Boolean,
    val isAvailableToday: Boolean,
    val homeServiceAvailable: Boolean,
    val medicalComplianceBadge: String? = null,
    val cancellationPolicy: String,
    val liveQueueCurrentServing: Int,
    val liveQueueNextToken: Int,
    val estimatedWaitPerPersonMin: Int,
    val accentHex: Long,
    val staff: List<StaffMemberProfile>,
    val services: List<MarketplaceServiceItem>,
    val offers: List<String>
)

data class LocationHub(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

data class CouponItem(
    val code: String,
    val title: String,
    val discountPercent: Int,
    val maxDiscountInr: Int,
    val minOrderInr: Int,
    val verticalTag: String
)

data class ArchitectureDeliverable(
    val number: Int,
    val title: String,
    val category: String,
    val summary: String,
    val keyHighlights: List<String>
)
