package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.integration.PaymentAndWhatsAppService
import com.example.data.model.AppRoleMode
import com.example.data.model.Appointment
import com.example.data.model.CouponItem
import com.example.data.model.GroomlyMarketplaceCatalog
import com.example.data.model.MarketplaceProvider
import com.example.data.model.MarketplaceServiceItem
import com.example.data.model.ProviderBusiness
import com.example.data.model.QueueTicket
import com.example.data.model.Review
import com.example.data.model.ServiceCategoryItem
import com.example.data.model.UserProfile
import com.example.data.repository.GroomlyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class BookingBreakdown(
    val subtotalInr: Int,
    val selfPackageDiscountPercent: Int,
    val selfPackageDiscountInr: Int,
    val couponDiscountInr: Int,
    val discountInr: Int,
    val netTotalInr: Int,
    val advancePayableInr: Int,
    val remainingBalanceInr: Int,
    val totalDurationMinutes: Int,
    val appliedCoupon: String
)

class GroomlyViewModel(
    private val repository: GroomlyRepository,
    val currentUserId: String,
    val userDisplayName: String
) : ViewModel() {

    // --- TWO-TIER FLOW ERROR HANDLING STREAMS ---
    val userProfileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile()
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing profile", e)
            emit(UiState.Error(e.message ?: "Failed to load profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val appointmentsState: StateFlow<UiState<List<Appointment>>> = repository.observeAppointments()
        .map<List<Appointment>, UiState<List<Appointment>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing appointments", e)
            emit(UiState.Error(e.message ?: "Failed to load appointments"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val queueTicketsState: StateFlow<UiState<List<QueueTicket>>> = repository.observeQueueTickets()
        .map<List<QueueTicket>, UiState<List<QueueTicket>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing queue tickets", e)
            emit(UiState.Error(e.message ?: "Failed to load live queue"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val providerBusinessesState: StateFlow<UiState<List<ProviderBusiness>>> = repository.observeProviderBusinesses()
        .map<List<ProviderBusiness>, UiState<List<ProviderBusiness>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing provider businesses", e)
            emit(UiState.Error(e.message ?: "Failed to load provider businesses"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    val reviewsState: StateFlow<UiState<List<Review>>> = repository.observeReviews()
        .map<List<Review>, UiState<List<Review>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing reviews", e)
            emit(UiState.Error(e.message ?: "Failed to load reviews"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), UiState.Loading)

    // --- UI & ECOSYSTEM NAVIGATION STATE ---
    private val _activeRoleMode = MutableStateFlow(AppRoleMode.CUSTOMER)
    val activeRoleMode: StateFlow<AppRoleMode> = _activeRoleMode.asStateFlow()

    private val _customerTabIndex = MutableStateFlow(0)
    val customerTabIndex: StateFlow<Int> = _customerTabIndex.asStateFlow()

    private val _providerTabIndex = MutableStateFlow(0)
    val providerTabIndex: StateFlow<Int> = _providerTabIndex.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // HTML5 Main Page Mode (Enabled by default per user suggestion #1)
    private val _isHtmlMainPageEnabled = MutableStateFlow(true)
    val isHtmlMainPageEnabled: StateFlow<Boolean> = _isHtmlMainPageEnabled.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // --- LOCATION-BASED BOOKING SYSTEM STATE ---
    private val _activeLocationLabel = MutableStateFlow("Indiranagar, Bengaluru")
    val activeLocationLabel: StateFlow<String> = _activeLocationLabel.asStateFlow()

    private val _userLatitude = MutableStateFlow(12.9784)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(77.6408)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    // --- MARKETPLACE DISCOVERY & ADMIN DYNAMIC STATE ---
    private val _categories = MutableStateFlow(GroomlyMarketplaceCatalog.initialCategories)
    val categories: StateFlow<List<ServiceCategoryItem>> = _categories.asStateFlow()

    private val _coupons = MutableStateFlow(GroomlyMarketplaceCatalog.coupons)
    val coupons: StateFlow<List<CouponItem>> = _coupons.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenderFilter = MutableStateFlow("ALL")
    val selectedGenderFilter: StateFlow<String> = _selectedGenderFilter.asStateFlow()

    private val _selectedVerticalFilter = MutableStateFlow("ALL")
    val selectedVerticalFilter: StateFlow<String> = _selectedVerticalFilter.asStateFlow()

    private val _onlyHomeService = MutableStateFlow(false)
    val onlyHomeService: StateFlow<Boolean> = _onlyHomeService.asStateFlow()

    private val _onlyAvailableToday = MutableStateFlow(false)
    val onlyAvailableToday: StateFlow<Boolean> = _onlyAvailableToday.asStateFlow()

    private val _isMapMode = MutableStateFlow(false)
    val isMapMode: StateFlow<Boolean> = _isMapMode.asStateFlow()

    // --- SELECTED PROVIDER DETAIL & SMART BOOKING SHEET ---
    private val _selectedProviderForDetail = MutableStateFlow<MarketplaceProvider?>(null)
    val selectedProviderForDetail: StateFlow<MarketplaceProvider?> = _selectedProviderForDetail.asStateFlow()

    // WhatsApp Business Notification Log
    private val _whatsappLogs = MutableStateFlow(
        listOf(
            "WhatsApp Cloud API [DELIVERED] • Welcome to Groomly! Self-Created Haircut Packages & UPI Pre-Charge active.",
            "WhatsApp Cloud API [DELIVERED] • Royal Gents Salon: Token #11 is now being served."
        )
    )
    val whatsappLogs: StateFlow<List<String>> = _whatsappLogs.asStateFlow()

    // Admin platform-wide default commission & subscription settings
    private val _defaultPlatformCommission = MutableStateFlow(15)
    val defaultPlatformCommission: StateFlow<Int> = _defaultPlatformCommission.asStateFlow()

    private val _monthlySaaSSubscriptionInr = MutableStateFlow(1499)
    val monthlySaaSSubscriptionInr: StateFlow<Int> = _monthlySaaSSubscriptionInr.asStateFlow()

    init {
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch {
            repository.ensureOrUpdateUserProfile(
                displayName = userDisplayName.ifBlank { "Aarav Sharma" }
            )
            val existingAppts = repository.getUserAppointments().getOrNull().orEmpty()
            if (existingAppts.isEmpty()) {
                repository.createAppointment(
                    providerId = "prov_royal_gents",
                    providerName = "Royal Gents Salon",
                    vertical = "SALON",
                    serviceSummary = "Self-Created Package: Executive Skin Fade + Hot Towel Beard Sculpt + Charcoal Facial",
                    staffId = "staff_ahmed",
                    staffName = "Ahmed • Senior Barber",
                    dateIso = "2026-11-05",
                    timeSlot = "11:45 AM",
                    durationMinutes = 90,
                    totalAmountInr = 750,
                    advancePaidInr = 150,
                    remainingBalanceInr = 600,
                    paymentMethod = "UPI",
                    paymentType = "ADVANCE",
                    couponCode = "BYOP18",
                    isHomeService = false
                )
                repository.joinLiveQueue(
                    providerId = "prov_royal_gents",
                    providerName = "Royal Gents Salon",
                    serviceName = "Executive Skin Fade & Wash",
                    staffName = "Ahmed • Senior Barber",
                    queueNumber = 14,
                    currentlyServingNumber = 11,
                    estimatedWaitMinutes = 24
                )
                repository.registerProviderBusiness(
                    ownerName = userDisplayName.ifBlank { "Ahmed Khan" },
                    businessName = "Royal Gents Flagship Studio",
                    category = "Men's Barber & Grooming Lounge",
                    vertical = "SALON",
                    genderFocus = "MENS",
                    cityArea = "100 Feet Road, Indiranagar, Bengaluru",
                    openingHours = "09:00 AM - 09:30 PM",
                    homeServiceEnabled = true,
                    autoAcceptBookings = true,
                    commissionRatePercent = 15,
                    subscriptionTier = "GROWTH"
                )
                repository.submitReview(
                    appointmentId = "seed_completed_1",
                    providerId = "prov_royal_gents",
                    providerName = "Royal Gents Salon",
                    staffName = "Ahmed • Senior Barber",
                    rating = 5,
                    staffRating = 5,
                    comment = "Built my own Haircut + Beard + Charcoal Facial package and pre-charged via UPI! Zero waiting at the chair.",
                    photoTag = "Self-Created Grooming Package"
                )
            }
        }
    }

    fun setRoleMode(mode: AppRoleMode) {
        _activeRoleMode.value = mode
    }

    fun setCustomerTab(index: Int) {
        _customerTabIndex.value = index
    }

    fun setProviderTab(index: Int) {
        _providerTabIndex.value = index
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleHtmlMainPage() {
        _isHtmlMainPageEnabled.value = !_isHtmlMainPageEnabled.value
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGenderFilter(gender: String) {
        _selectedGenderFilter.value = gender
    }

    fun setVerticalFilter(vertical: String) {
        _selectedVerticalFilter.value = vertical
    }

    fun toggleHomeServiceFilter() {
        _onlyHomeService.value = !_onlyHomeService.value
    }

    fun toggleAvailableTodayFilter() {
        _onlyAvailableToday.value = !_onlyAvailableToday.value
    }

    fun toggleMapMode() {
        _isMapMode.value = !_isMapMode.value
    }

    fun selectProviderForDetail(provider: MarketplaceProvider?) {
        _selectedProviderForDetail.value = provider
    }

    // --- LOCATION-BASED BOOKING SYSTEM ---
    fun updateActiveGpsCoordinates(cityLabel: String, latitude: Double, longitude: Double) {
        _activeLocationLabel.value = cityLabel
        _userLatitude.value = latitude
        _userLongitude.value = longitude
        topUpWalletAndPreferences(newCity = cityLabel)
        _statusBannerMessage.value =
            "📍 Location Locked: $cityLabel (${String.format("%.3f", latitude)}°N, ${String.format("%.3f", longitude)}°E)"
    }

    fun getLocationSortedProviders(): List<MarketplaceProvider> {
        val lat = _userLatitude.value
        val lng = _userLongitude.value
        return GroomlyMarketplaceCatalog.providers.map { p ->
            val dynamicDist = GroomlyMarketplaceCatalog.calculateHaversineKm(lat, lng, p.latitude, p.longitude)
            p.copy(distanceKm = dynamicDist)
        }.sortedBy { it.distanceKm }
    }

    // --- SELF-CREATED PACKAGE BUILDER & PRE-CHARGE CALCULATOR ---
    fun calculateBookingBreakdown(
        selectedServices: List<MarketplaceServiceItem>,
        staffAddonInr: Int,
        couponCode: String,
        paymentType: String, // ADVANCE (20%), HALF (50%), FULL (100%), PAY_AT_VENUE
        prechargeCustomPercent: Int = 20
    ): BookingBreakdown {
        val rawServicesTotal = selectedServices.sumOf { it.discountPriceInr }
        val subtotal = (rawServicesTotal + staffAddonInr).coerceAtLeast(0)

        // Self-Created Custom Package Tier Discount (when customer combines Haircut + other services)
        val selfPkgPercent = when {
            selectedServices.size >= 4 -> 20
            selectedServices.size == 3 -> 15
            selectedServices.size == 2 -> 10
            else -> 0
        }
        val selfPkgDiscountInr = (subtotal * selfPkgPercent) / 100
        val afterSelfPkg = (subtotal - selfPkgDiscountInr).coerceAtLeast(0)

        val matchedCoupon = _coupons.value.firstOrNull {
            it.code.equals(couponCode.trim(), ignoreCase = true) && afterSelfPkg >= it.minOrderInr
        }
        val couponDiscount = if (matchedCoupon != null) {
            ((afterSelfPkg * matchedCoupon.discountPercent) / 100).coerceAtMost(matchedCoupon.maxDiscountInr)
        } else {
            0
        }
        val totalDiscount = selfPkgDiscountInr + couponDiscount
        val netTotal = (subtotal - totalDiscount).coerceAtLeast(0)

        val advancePayable = when (paymentType) {
            "FULL" -> netTotal
            "PAY_AT_VENUE" -> 0
            else -> ((netTotal * prechargeCustomPercent) / 100)
                .coerceAtLeast(if (netTotal > 0) 100.coerceAtMost(netTotal) else 0)
        }
        val remaining = (netTotal - advancePayable).coerceAtLeast(0)
        val totalDuration = selectedServices.sumOf { it.durationMinutes + it.bufferMinutes }.coerceAtLeast(15)
        return BookingBreakdown(
            subtotalInr = subtotal,
            selfPackageDiscountPercent = selfPkgPercent,
            selfPackageDiscountInr = selfPkgDiscountInr,
            couponDiscountInr = couponDiscount,
            discountInr = totalDiscount,
            netTotalInr = netTotal,
            advancePayableInr = advancePayable,
            remainingBalanceInr = remaining,
            totalDurationMinutes = totalDuration,
            appliedCoupon = matchedCoupon?.code ?: if (selfPkgPercent > 0) "SELF_PKG_${selfPkgPercent}" else "NONE"
        )
    }

    fun getAvailableTimeSlots(
        providerId: String,
        staffId: String,
        dateIso: String,
        existingAppointments: List<Appointment>
    ): List<Pair<String, Boolean>> {
        val bookedSlots = existingAppointments
            .filter {
                it.providerId == providerId &&
                    it.dateIso == dateIso &&
                    (staffId == "any_available" || it.staffId == staffId) &&
                    it.status !in listOf("CANCELLED", "REFUNDED")
            }
            .map { it.timeSlot }
            .toSet()

        return GroomlyMarketplaceCatalog.standardTimeSlots.map { slot ->
            slot to !bookedSlots.contains(slot)
        }
    }

    // --- BOOKING & SELF-CREATED PACKAGE ACTIONS ---
    fun bookSelfCreatedCustomPackage(
        providerId: String,
        providerName: String,
        serviceSummary: String,
        durationMinutes: Int,
        netTotalInr: Int,
        prechargedAdvanceInr: Int,
        remainingBalanceInr: Int,
        paymentGateway: String
    ) {
        val mappedMethod = when (paymentGateway.uppercase()) {
            "UPI" -> "UPI"
            "RAZORPAY", "STRIPE_CARD", "CARD" -> "CARD"
            "NET_BANKING" -> "NET_BANKING"
            "WALLET" -> "WALLET"
            else -> "UPI"
        }
        val mappedType = if (remainingBalanceInr == 0) "FULL" else "ADVANCE"

        viewModelScope.launch {
            repository.createAppointment(
                providerId = providerId,
                providerName = providerName,
                vertical = "SALON",
                serviceSummary = serviceSummary,
                staffId = "staff_ahmed",
                staffName = "Ahmed • Senior Barber",
                dateIso = "2026-11-05",
                timeSlot = "11:00 AM",
                durationMinutes = durationMinutes,
                totalAmountInr = netTotalInr,
                advancePaidInr = prechargedAdvanceInr,
                remainingBalanceInr = remainingBalanceInr,
                paymentMethod = mappedMethod,
                paymentType = mappedType,
                couponCode = "SELF_CREATED_PKG",
                isHomeService = false
            ).onSuccess {
                val receipt = PaymentAndWhatsAppService.buildGatewayAuthorizationReceipt(
                    gateway = paymentGateway,
                    amountInr = prechargedAdvanceInr,
                    payerIdentifier = userDisplayName.ifBlank { "Customer" }
                )
                val waStatus = PaymentAndWhatsAppService.dispatchWhatsAppCloudMessage(
                    recipientPhone = "+919845011223",
                    messageBody = "Groomly Custom Package Confirmed at $providerName ($serviceSummary). Pre-charged ₹$prechargedAdvanceInr, Balance ₹$remainingBalanceInr."
                )
                appendWhatsAppLog(waStatus)
                _statusBannerMessage.value =
                    "✅ Custom Package Booked! $receipt • Balance at Salon: ₹$remainingBalanceInr"
            }
        }
    }

    fun confirmSmartAppointment(
        provider: MarketplaceProvider,
        selectedServices: List<MarketplaceServiceItem>,
        staffId: String,
        staffName: String,
        staffAddonInr: Int,
        dateIso: String,
        timeSlot: String,
        couponCode: String,
        paymentMethod: String,
        paymentType: String,
        prechargeCustomPercent: Int = 20,
        isHomeService: Boolean,
        onComplete: () -> Unit
    ) {
        val breakdown = calculateBookingBreakdown(
            selectedServices = selectedServices,
            staffAddonInr = staffAddonInr,
            couponCode = couponCode,
            paymentType = paymentType,
            prechargeCustomPercent = prechargeCustomPercent
        )
        val prefix = if (selectedServices.size > 1) "Self-Created Package: " else ""
        val summary = (prefix + selectedServices.joinToString(" + ") { it.name.substringBefore(" (") })
            .ifBlank { "Custom Service" }

        viewModelScope.launch {
            val result = repository.createAppointment(
                providerId = provider.id,
                providerName = provider.name,
                vertical = provider.vertical,
                serviceSummary = summary,
                staffId = staffId,
                staffName = staffName,
                dateIso = dateIso,
                timeSlot = timeSlot,
                durationMinutes = breakdown.totalDurationMinutes,
                totalAmountInr = breakdown.netTotalInr,
                advancePaidInr = breakdown.advancePayableInr,
                remainingBalanceInr = breakdown.remainingBalanceInr,
                paymentMethod = paymentMethod,
                paymentType = if (paymentType == "HALF") "ADVANCE" else paymentType,
                couponCode = breakdown.appliedCoupon,
                isHomeService = isHomeService
            )
            result.onSuccess {
                val waLog = PaymentAndWhatsAppService.dispatchWhatsAppCloudMessage(
                    recipientPhone = provider.contactPhone,
                    messageBody = "${provider.name} on $dateIso at $timeSlot ($summary). Pre-Charged ₹${breakdown.advancePayableInr} ($paymentMethod), Balance ₹${breakdown.remainingBalanceInr}."
                )
                appendWhatsAppLog(waLog)
                _statusBannerMessage.value =
                    "Booking Confirmed! Pre-Charged ₹${breakdown.advancePayableInr} • Balance ₹${breakdown.remainingBalanceInr}"
                onComplete()
            }.onFailure { e ->
                _statusBannerMessage.value = e.localizedMessage ?: "Failed to confirm booking"
            }
        }
    }

    fun dispatchWhatsAppAndOpenChat(
        context: Context,
        phone: String,
        message: String
    ) {
        PaymentAndWhatsAppService.openWhatsAppDeepLink(context, phone, message)
        viewModelScope.launch {
            val logEntry = PaymentAndWhatsAppService.dispatchWhatsAppCloudMessage(phone, message)
            appendWhatsAppLog(logEntry)
            _statusBannerMessage.value = "WhatsApp Confirmation Dispatched!"
        }
    }

    fun rescheduleExistingAppointment(
        appointment: Appointment,
        newDateIso: String,
        newTimeSlot: String
    ) {
        viewModelScope.launch {
            repository.rescheduleAppointment(
                appointmentId = appointment.id,
                newDateIso = newDateIso,
                newTimeSlot = newTimeSlot,
                newStaffId = appointment.staffId,
                newStaffName = appointment.staffName
            ).onSuccess {
                appendWhatsAppLog(
                    "WhatsApp Cloud API [RESCHEDULED] • ${appointment.providerName} moved to $newDateIso at $newTimeSlot."
                )
                _statusBannerMessage.value = "Appointment rescheduled to $newDateIso at $newTimeSlot"
            }
        }
    }

    fun updateAppointmentLifecycle(appointment: Appointment, newStatus: String) {
        viewModelScope.launch {
            val newAdvance = if (newStatus == "COMPLETED") appointment.totalAmountInr else appointment.advancePaidInr
            val newRemaining = if (newStatus in listOf("COMPLETED", "CANCELLED", "REFUNDED")) 0L else appointment.remainingBalanceInr
            repository.updateAppointmentStatus(
                appointmentId = appointment.id,
                newStatus = newStatus,
                advancePaidInr = newAdvance,
                remainingBalanceInr = newRemaining
            ).onSuccess {
                appendWhatsAppLog(
                    "WhatsApp Cloud API [STATUS: $newStatus] • ${appointment.providerName} (${appointment.serviceSummary})."
                )
                _statusBannerMessage.value = "Appointment marked $newStatus"
            }
        }
    }

    fun joinProviderLiveQueue(
        provider: MarketplaceProvider,
        serviceName: String,
        staffName: String
    ) {
        viewModelScope.launch {
            val currentTickets = (queueTicketsState.value as? UiState.Success)?.data.orEmpty()
            val nextNum = (provider.liveQueueNextToken + currentTickets.size)
            val waitMins = (nextNum - provider.liveQueueCurrentServing).coerceAtLeast(1) * provider.estimatedWaitPerPersonMin

            repository.joinLiveQueue(
                providerId = provider.id,
                providerName = provider.name,
                serviceName = serviceName,
                staffName = staffName,
                queueNumber = nextNum,
                currentlyServingNumber = provider.liveQueueCurrentServing,
                estimatedWaitMinutes = waitMins
            ).onSuccess {
                appendWhatsAppLog(
                    "WhatsApp Cloud API [QUEUE TICKET #$nextNum] • Joined ${provider.name} live queue. Est. wait ~$waitMins mins (non-guaranteed)."
                )
                _statusBannerMessage.value = "Joined Live Queue! Digital Token #$nextNum issued."
            }
        }
    }

    fun advanceOrCheckInQueueTicket(ticket: QueueTicket, action: String) {
        viewModelScope.launch {
            val (nextServing, nextWait, nextStatus) = when (action) {
                "QR_CHECK_IN" -> Triple(
                    ticket.currentlyServingNumber.toInt(),
                    ticket.estimatedWaitMinutes.toInt().coerceAtMost(5),
                    "CHECKED_IN"
                )
                "ADVANCE_TURN" -> {
                    val newServing = (ticket.currentlyServingNumber + 1).coerceAtMost(ticket.queueNumber).toInt()
                    val remaining = (ticket.queueNumber - newServing).toInt().coerceAtLeast(0)
                    val status = when {
                        remaining == 0 -> "SERVING"
                        remaining == 1 -> "APPROACHING"
                        else -> ticket.status
                    }
                    Triple(newServing, remaining * 8, status)
                }
                "COMPLETE" -> Triple(ticket.queueNumber.toInt(), 0, "COMPLETED")
                "CANCEL" -> Triple(ticket.currentlyServingNumber.toInt(), 0, "CANCELLED")
                else -> Triple(ticket.currentlyServingNumber.toInt(), ticket.estimatedWaitMinutes.toInt(), ticket.status)
            }
            repository.updateQueueTicketState(
                ticketId = ticket.id,
                currentlyServingNumber = nextServing,
                estimatedWaitMinutes = nextWait,
                newStatus = nextStatus
            ).onSuccess {
                appendWhatsAppLog(
                    "WhatsApp Cloud API [QUEUE UPDATE] • Token #${ticket.queueNumber} at ${ticket.providerName} is now $nextStatus."
                )
                _statusBannerMessage.value = "Queue Token #${ticket.queueNumber} -> $nextStatus"
            }
        }
    }

    // --- CUSTOMER PROFILE, FAVOURITES & WALLET ---
    fun toggleFavouriteProvider(providerId: String) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(userId = currentUserId)
        val currentFavs = currentProfile.favouriteProviderIds.toMutableList()
        if (currentFavs.contains(providerId)) {
            currentFavs.remove(providerId)
        } else if (currentFavs.size < 10) {
            currentFavs.add(providerId)
        }
        viewModelScope.launch {
            repository.ensureOrUpdateUserProfile(
                displayName = currentProfile.displayName.ifBlank { userDisplayName },
                genderPreference = currentProfile.genderPreference,
                cityLocation = currentProfile.cityLocation,
                savedAddresses = currentProfile.savedAddresses,
                favouriteProviderIds = currentFavs,
                walletBalanceInr = currentProfile.walletBalanceInr,
                loyaltyPoints = currentProfile.loyaltyPoints,
                whatsappAlertsEnabled = currentProfile.whatsappAlertsEnabled,
                pushNotificationsEnabled = currentProfile.pushNotificationsEnabled,
                marketingConsent = currentProfile.marketingConsent
            )
        }
    }

    fun topUpWalletAndPreferences(
        addWalletInr: Long = 0L,
        newCity: String? = null,
        newAddress: String? = null,
        whatsappEnabled: Boolean? = null
    ) {
        val currentProfile = (userProfileState.value as? UiState.Success)?.data ?: UserProfile(userId = currentUserId)
        val addresses = if (!newAddress.isNullOrBlank() && currentProfile.savedAddresses.size < 10) {
            currentProfile.savedAddresses + newAddress.trim()
        } else {
            currentProfile.savedAddresses
        }
        viewModelScope.launch {
            repository.ensureOrUpdateUserProfile(
                displayName = currentProfile.displayName.ifBlank { userDisplayName },
                genderPreference = currentProfile.genderPreference,
                cityLocation = newCity ?: currentProfile.cityLocation,
                savedAddresses = addresses,
                favouriteProviderIds = currentProfile.favouriteProviderIds,
                walletBalanceInr = (currentProfile.walletBalanceInr + addWalletInr).coerceIn(0L, 1000000L),
                loyaltyPoints = currentProfile.loyaltyPoints + if (addWalletInr > 0) 25L else 0L,
                whatsappAlertsEnabled = whatsappEnabled ?: currentProfile.whatsappAlertsEnabled,
                pushNotificationsEnabled = currentProfile.pushNotificationsEnabled,
                marketingConsent = currentProfile.marketingConsent
            ).onSuccess {
                if (addWalletInr > 0) {
                    _statusBannerMessage.value = "Added ₹$addWalletInr to Groomly Wallet (+25 Loyalty Pts)"
                }
            }
        }
    }

    // --- VERIFIED REVIEWS ---
    fun submitVerifiedReview(
        appointment: Appointment,
        rating: Int,
        staffRating: Int,
        comment: String,
        photoTag: String
    ) {
        viewModelScope.launch {
            repository.submitReview(
                appointmentId = appointment.id,
                providerId = appointment.providerId,
                providerName = appointment.providerName,
                staffName = appointment.staffName,
                rating = rating,
                staffRating = staffRating,
                comment = comment,
                photoTag = photoTag
            ).onSuccess {
                _statusBannerMessage.value = "Verified Review published for ${appointment.providerName}!"
            }
        }
    }

    fun toggleReportReview(review: Review) {
        viewModelScope.launch {
            repository.toggleReportReview(review).onSuccess {
                _statusBannerMessage.value =
                    if (!review.isReported) "Review flagged for Super Admin moderation" else "Review flag cleared"
            }
        }
    }

    // --- PROVIDER REGISTRATION & ADMIN GOVERNANCE ---
    fun registerNewProviderBusiness(
        ownerName: String,
        businessName: String,
        category: String,
        vertical: String,
        genderFocus: String,
        cityArea: String,
        openingHours: String,
        homeServiceEnabled: Boolean
    ) {
        viewModelScope.launch {
            repository.registerProviderBusiness(
                ownerName = ownerName,
                businessName = businessName,
                category = category,
                vertical = vertical,
                genderFocus = genderFocus,
                cityArea = cityArea,
                openingHours = openingHours,
                homeServiceEnabled = homeServiceEnabled,
                autoAcceptBookings = true,
                commissionRatePercent = _defaultPlatformCommission.value,
                subscriptionTier = "GROWTH"
            ).onSuccess {
                _statusBannerMessage.value = "$businessName submitted for Admin Verification!"
            }
        }
    }

    fun updateProviderBusinessGovernance(
        business: ProviderBusiness,
        newVerificationStatus: String = business.verificationStatus,
        newCommissionPercent: Int = business.commissionRatePercent.toInt(),
        newSubscriptionTier: String = business.subscriptionTier,
        autoAccept: Boolean = business.autoAcceptBookings,
        homeService: Boolean = business.homeServiceEnabled
    ) {
        viewModelScope.launch {
            repository.updateProviderGovernance(
                business = business,
                verificationStatus = newVerificationStatus,
                commissionRatePercent = newCommissionPercent,
                subscriptionTier = newSubscriptionTier,
                autoAcceptBookings = autoAccept,
                homeServiceEnabled = homeService
            ).onSuccess {
                _statusBannerMessage.value =
                    "Updated ${business.businessName}: $newVerificationStatus • ${newCommissionPercent}% Commission"
            }
        }
    }

    fun addMarketplaceCategory(name: String, vertical: String, genderTag: String, startingPriceInr: Int) {
        if (name.isBlank()) return
        val newCat = ServiceCategoryItem(
            id = "cat_${_categories.value.size + 1}",
            name = name.trim(),
            vertical = vertical,
            genderTag = genderTag,
            startingPriceInr = startingPriceInr.coerceAtLeast(99)
        )
        _categories.value = _categories.value + newCat
        _statusBannerMessage.value = "Category '${newCat.name}' added to Marketplace"
    }

    fun removeMarketplaceCategory(categoryId: String) {
        _categories.value = _categories.value.filterNot { it.id == categoryId }
        _statusBannerMessage.value = "Category removed from Marketplace"
    }

    fun updateAdminMonetizationConfig(commissionPercent: Int, monthlySubInr: Int) {
        _defaultPlatformCommission.value = commissionPercent.coerceIn(5, 40)
        _monthlySaaSSubscriptionInr.value = monthlySubInr.coerceIn(499, 9999)
        _statusBannerMessage.value =
            "Platform Monetization Updated: ${_defaultPlatformCommission.value}% Commission • ₹${_monthlySaaSSubscriptionInr.value}/mo SaaS"
    }

    private fun appendWhatsAppLog(entry: String) {
        _whatsappLogs.value = listOf(entry) + _whatsappLogs.value.take(14)
    }

    private companion object {
        const val TAG = "GroomlyVM"
        const val STOP_TIMEOUT = 5000L
    }
}
