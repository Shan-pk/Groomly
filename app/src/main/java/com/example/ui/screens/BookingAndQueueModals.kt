package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.MarketplaceProvider
import com.example.data.model.MarketplaceServiceItem
import com.example.data.model.QueueTicket
import com.example.data.model.Review
import com.example.ui.theme.GroomlyElectricLime
import com.example.ui.theme.GroomlyMedicalCyan
import com.example.ui.theme.GroomlyObsidian
import com.example.ui.theme.GroomlySuccessMint
import com.example.ui.theme.GroomlyVibrantViolet
import com.example.ui.viewmodel.GroomlyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProviderDetailAndBookingScreen(
    provider: MarketplaceProvider,
    existingAppointments: List<Appointment>,
    reviews: List<Review>,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    onBack: () -> Unit,
    viewModel: GroomlyViewModel
) {
    BackHandler { onBack() }

    val selectedServices = remember(provider.id) {
        mutableStateListOf<MarketplaceServiceItem>().apply {
            provider.services.firstOrNull()?.let { add(it) }
        }
    }
    var selectedStaffId by remember(provider.id) {
        mutableStateOf(provider.staff.firstOrNull()?.id ?: "any_available")
    }
    val dateOptions = remember {
        listOf("2026-11-04", "2026-11-05", "2026-11-06", "2026-11-07")
    }
    var selectedDate by remember { mutableStateOf(dateOptions.first()) }

    val slotPairs = remember(provider.id, selectedStaffId, selectedDate, existingAppointments) {
        viewModel.getAvailableTimeSlots(provider.id, selectedStaffId, selectedDate, existingAppointments)
    }
    var selectedTimeSlot by remember(slotPairs) {
        mutableStateOf(slotPairs.firstOrNull { it.second }?.first ?: "11:00 AM")
    }
    var couponInput by remember {
        mutableStateOf(
            when (provider.vertical) {
                "BRIDAL" -> "BRIDALGLOW"
                "DERMATOLOGY", "DIETITIAN" -> "CLINIC15"
                else -> "GROOM100"
            }
        )
    }
    var selectedPaymentType by remember { mutableStateOf("ADVANCE") } // ADVANCE, FULL, PAY_AT_VENUE
    var selectedPaymentMethod by remember { mutableStateOf("UPI") } // UPI, CARD, NET_BANKING, WALLET, CASH_AT_SHOP
    var bookAsHomeService by remember { mutableStateOf(false) }

    val selectedStaffObj = provider.staff.firstOrNull { it.id == selectedStaffId }
    val staffAddon = selectedStaffObj?.priceAddonInr ?: 0
    val staffDisplayName = if (selectedStaffObj != null) {
        "${selectedStaffObj.name} • ${selectedStaffObj.roleTitle}"
    } else {
        "Any Available Professional"
    }

    val breakdown = viewModel.calculateBookingBreakdown(
        selectedServices = selectedServices.toList(),
        staffAddonInr = staffAddon,
        couponCode = couponInput,
        paymentType = selectedPaymentType
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_from_provider_detail")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Marketplace")
            }
            Text(
                text = provider.vertical + " PROFILE & SMART BOOKING",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = onToggleFavourite) {
                Text(if (isFavourite) "♥ Saved" else "♡ Save")
            }
        }

        // Business Hero Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(provider.accentHex),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = provider.genderFocus,
                            color = GroomlyObsidian,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (provider.isVerified) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Provider",
                                tint = GroomlySuccessMint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified Partner",
                                style = MaterialTheme.typography.labelMedium,
                                color = GroomlySuccessMint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = provider.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (provider.medicalComplianceBadge != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = GroomlyMedicalCyan.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = "Medical Verification",
                                tint = GroomlyMedicalCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = provider.medicalComplianceBadge,
                                style = MaterialTheme.typography.labelMedium,
                                color = GroomlyMedicalCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = provider.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "📍 ${provider.address} (${provider.distanceKm} km) • 🕒 ${provider.openingHours}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "★ ${provider.rating} (${provider.reviewCount} verified reviews) • 📞 ${provider.contactPhone}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Queue Quick Join Banner inside Provider Profile
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIVE WALK-IN QUEUE • Serving #${provider.liveQueueCurrentServing}",
                                style = MaterialTheme.typography.labelLarge,
                                color = GroomlySuccessMint,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Next Token #${provider.liveQueueNextToken} • Est. ~${provider.estimatedWaitPerPersonMin * 2}m wait (non-guaranteed)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.joinProviderLiveQueue(
                                    provider = provider,
                                    serviceName = selectedServices.firstOrNull()?.name ?: provider.services.first().name,
                                    staffName = staffDisplayName
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("join_live_queue_button")
                        ) {
                            Text("Join Queue")
                        }
                    }
                }
            }
        }

        // STEP 1: Select Services & Bundles
        Text(
            text = "1. Select Services or Packages (Multi-Select Enabled)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        provider.services.forEach { service ->
            val isChecked = selectedServices.any { it.id == service.id }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isChecked && selectedServices.size > 1) {
                            selectedServices.removeAll { it.id == service.id }
                        } else if (!isChecked) {
                            selectedServices.add(service)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            if (isChecked && selectedServices.size > 1) {
                                selectedServices.removeAll { s -> s.id == service.id }
                            } else if (!isChecked) {
                                selectedServices.add(service)
                            }
                        }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = service.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (service.isBundle) {
                                Surface(
                                    color = GroomlyVibrantViolet,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "BUNDLE",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = service.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (service.bundleIncludedItems.isNotEmpty()) {
                            Text(
                                text = "Includes: " + service.bundleIncludedItems.joinToString(" • "),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${service.durationMinutes} mins (+${service.bufferMinutes}m prep buffer)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${service.discountPriceInr}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (service.priceInr > service.discountPriceInr) {
                            Text(
                                text = "MRP ₹${service.priceInr}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // STEP 2: Select Staff Member or Any Available
        Text(
            text = "2. Choose Specialist or Any Available",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedStaffId == "any_available",
                onClick = { selectedStaffId = "any_available" },
                label = { Text("Any Available (+₹0)") }
            )
            provider.staff.forEach { staff ->
                val addonText = if (staff.priceAddonInr > 0) " (+₹${staff.priceAddonInr})" else ""
                FilterChip(
                    selected = selectedStaffId == staff.id,
                    onClick = { selectedStaffId = staff.id },
                    label = { Text("${staff.name} • ${staff.roleTitle}$addonText") }
                )
            }
        }

        selectedStaffObj?.let { staff ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${staff.name} (${staff.roleTitle}) • ★ ${staff.rating} (${staff.completedJobs} bookings)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Specialty: ${staff.specialty} • Shift: ${staff.shiftHours} (Break: ${staff.breakWindow})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (staff.portfolioHighlights.isNotEmpty()) {
                        Text(
                            text = "Portfolio: " + staff.portfolioHighlights.joinToString(" | "),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // STEP 3: Select Date & Conflict-Checked Slot
        Text(
            text = "3. Select Date & Conflict-Validated Slot",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dateOptions.forEach { dateStr ->
                FilterChip(
                    selected = selectedDate == dateStr,
                    onClick = { selectedDate = dateStr },
                    label = { Text(dateStr) }
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slotPairs.forEach { (slot, isAvailable) ->
                FilterChip(
                    selected = selectedTimeSlot == slot,
                    enabled = isAvailable,
                    onClick = { if (isAvailable) selectedTimeSlot = slot },
                    label = {
                        Text(if (isAvailable) slot else "$slot (Booked)")
                    }
                )
            }
        }

        // Home Service Option if supported
        if (provider.homeServiceAvailable) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = "Home Service", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Book as Doorstep Home Service", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Professional arrives at your saved address with sanitized kit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(checked = bookAsHomeService, onCheckedChange = { bookAsHomeService = it })
            }
        }

        // STEP 4: Coupon & Payment Split
        Text(
            text = "4. Coupon & India-First Payment Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = couponInput,
            onValueChange = { couponInput = it.uppercase() },
            label = { Text("Promo Code (Try GROOM100, LIME20, BRIDALGLOW, CLINIC15)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ADVANCE" to "20% Advance Lock",
                "FULL" to "100% Full Online",
                "PAY_AT_VENUE" to "Cash / Pay at Shop"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = selectedPaymentType == key,
                    onClick = { selectedPaymentType = key },
                    label = { Text(label) }
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("UPI", "CARD", "NET_BANKING", "WALLET", "CASH_AT_SHOP").forEach { method ->
                FilterChip(
                    selected = selectedPaymentMethod == method,
                    onClick = { selectedPaymentMethod = method },
                    label = { Text(method.replace("_", " ")) }
                )
            }
        }

        // Payment Breakdown Card (Exact Prompt Logic: Service ₹500 -> Advance ₹100 -> Remaining ₹400)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "SMART BOOKING & PAYMENT BREAKDOWN",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                BreakdownRow("Total Duration (incl. buffer)", "${breakdown.totalDurationMinutes} mins")
                BreakdownRow("Service & Specialist Subtotal", "₹${breakdown.subtotalInr}")
                if (breakdown.discountInr > 0) {
                    BreakdownRow("Coupon Discount (${breakdown.appliedCoupon})", "- ₹${breakdown.discountInr}")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                BreakdownRow("Net Service Amount", "₹${breakdown.netTotalInr}", bold = true)
                BreakdownRow("Advance Payable Now ($selectedPaymentMethod)", "₹${breakdown.advancePayableInr}", highlight = true)
                BreakdownRow("Remaining Balance at Venue", "₹${breakdown.remainingBalanceInr}", bold = true)
                Text(
                    text = provider.cancellationPolicy,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = {
                viewModel.confirmSmartAppointment(
                    provider = provider,
                    selectedServices = selectedServices.toList(),
                    staffId = selectedStaffId,
                    staffName = staffDisplayName,
                    staffAddonInr = staffAddon,
                    dateIso = selectedDate,
                    timeSlot = selectedTimeSlot,
                    couponCode = couponInput,
                    paymentMethod = selectedPaymentMethod,
                    paymentType = selectedPaymentType,
                    isHomeService = bookAsHomeService,
                    onComplete = onBack
                )
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("confirm_smart_booking_button")
        ) {
            Text(
                text = "Confirm Booking • Pay ₹${breakdown.advancePayableInr} Now (Balance ₹${breakdown.remainingBalanceInr})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Verified Reviews Section
        Text(
            text = "Verified Customer Reviews",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        val providerReviews = reviews.filter { it.providerId == provider.id }
        if (providerReviews.isEmpty()) {
            Text(
                text = "Completed bookings unlock verified star ratings & staff reviews.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            providerReviews.forEach { rev ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "★ ${rev.rating}/5 • Staff (${rev.staffName}): ★ ${rev.staffRating}/5",
                                style = MaterialTheme.typography.labelLarge,
                                color = GroomlySuccessMint
                            )
                            Text(
                                text = "Verified Booking",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = rev.comment, style = MaterialTheme.typography.bodyMedium)
                        if (rev.photoTag.isNotBlank()) {
                            Text(
                                text = "📸 Photo Tag: ${rev.photoTag}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    bold: Boolean = false,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold || highlight) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold || highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LiveQueueQrDialog(
    ticket: QueueTicket,
    onDismiss: () -> Unit,
    onCheckIn: () -> Unit,
    onAdvanceTurn: () -> Unit,
    onCancelTicket: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Digital Queue Ticket #${ticket.queueNumber}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${ticket.providerName} • ${ticket.serviceName}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Currently Serving: #${ticket.currentlyServingNumber} • Status: ${ticket.status}",
                    style = MaterialTheme.typography.labelLarge,
                    color = GroomlySuccessMint
                )
                // Custom Canvas QR Matrix
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val grid = 7
                        val cell = size.width / grid
                        val seed = ticket.qrCheckInCode.hashCode()
                        for (r in 0 until grid) {
                            for (c in 0 until grid) {
                                val isCorner = (r < 2 && c < 2) || (r < 2 && c >= grid - 2) || (r >= grid - 2 && c < 2)
                                val bit = ((seed shr ((r * grid + c) % 28)) and 1) == 1
                                if (isCorner || bit) {
                                    drawRect(
                                        color = Color(0xFF0D0E11),
                                        topLeft = Offset(c * cell, r * cell),
                                        size = Size(cell * 0.88f, cell * 0.88f)
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    text = "QR Check-In Code: ${ticket.qrCheckInCode}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Estimated wait ~${ticket.estimatedWaitMinutes} mins. Note: Wait times are real-time estimates and are not guaranteed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onAdvanceTurn,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simulate Next")
                    }
                    Button(
                        onClick = onCheckIn,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("QR Check-In")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            if (ticket.status !in listOf("COMPLETED", "CANCELLED")) {
                TextButton(onClick = onCancelTicket) {
                    Text("Leave Queue", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}
