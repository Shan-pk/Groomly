package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.integration.PaymentAndWhatsAppService
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
    val context = LocalContext.current

    val selectedServices = remember(provider.id) {
        mutableStateListOf<MarketplaceServiceItem>().apply {
            provider.services.take(2).forEach { add(it) }
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
                else -> "BYOP18"
            }
        )
    }
    var prechargePercent by remember { mutableIntStateOf(20) } // 20%, 50%, 100%, or 0%
    var selectedPaymentType by remember { mutableStateOf("ADVANCE") } // ADVANCE, FULL, PAY_AT_VENUE
    var selectedPaymentMethod by remember { mutableStateOf("UPI") } // UPI, CARD, NET_BANKING, WALLET, CASH_AT_SHOP
    var bookAsHomeService by remember { mutableStateOf(false) }
    var showGatewayModal by remember { mutableStateOf(false) }

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
        paymentType = selectedPaymentType,
        prechargeCustomPercent = prechargePercent
    )

    if (showGatewayModal) {
        MultiGatewayPaymentDialog(
            amountInr = breakdown.advancePayableInr,
            initialGateway = selectedPaymentMethod,
            serviceDescription = selectedServices.joinToString(" + ") { it.name.substringBefore(" (") },
            onDismiss = { showGatewayModal = false },
            onLaunchUpiApp = {
                PaymentAndWhatsAppService.launchUpiPaymentIntent(
                    context = context,
                    amountInr = breakdown.advancePayableInr,
                    merchantName = provider.name,
                    transactionNote = "Groomly Self-Created Package"
                )
            },
            onPaymentAuthorized = { chosenMethod ->
                selectedPaymentMethod = chosenMethod
                showGatewayModal = false
                viewModel.confirmSmartAppointment(
                    provider = provider,
                    selectedServices = selectedServices.toList(),
                    staffId = selectedStaffId,
                    staffName = staffDisplayName,
                    staffAddonInr = staffAddon,
                    dateIso = selectedDate,
                    timeSlot = selectedTimeSlot,
                    couponCode = couponInput,
                    paymentMethod = chosenMethod,
                    paymentType = selectedPaymentType,
                    prechargeCustomPercent = prechargePercent,
                    isHomeService = bookAsHomeService,
                    onComplete = onBack
                )
            }
        )
    }

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
                text = provider.vertical + " • CUSTOM PACKAGE STUDIO",
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
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
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
                            text = "${provider.genderFocus} • 📍 ${provider.distanceKm} km",
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
                                text = "MNC Verified Partner",
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
                Text(text = provider.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📍 ${provider.address} • 🕒 ${provider.openingHours} • ★ ${provider.rating} (${provider.reviewCount})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Queue + Direct WhatsApp Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.joinProviderLiveQueue(
                                provider = provider,
                                serviceName = selectedServices.firstOrNull()?.name ?: provider.services.first().name,
                                staffName = staffDisplayName
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("join_live_queue_button")
                    ) {
                        Text("Join Queue (#${provider.liveQueueNextToken})")
                    }
                    OutlinedButton(
                        onClick = {
                            val pkgSummary = selectedServices.joinToString(", ") { it.name.substringBefore(" (") }
                            viewModel.dispatchWhatsAppAndOpenChat(
                                context = context,
                                phone = provider.contactPhone,
                                message = "Hi ${provider.name}! I'm creating my custom Groomly package ($pkgSummary) on $selectedDate at $selectedTimeSlot."
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("provider_whatsapp_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp Desk")
                    }
                }
            }
        }

        // STEP 1: SELF-CREATED CUSTOM PACKAGING BUILDER (BYOP)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Self-Created Package",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "1. Self-Created Custom Package Builder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Coming for a Haircut? Add any other shop services below according to your wishes. Multi-service packages unlock automatic bundle discounts & custom pre-charging!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val tierProgress = (selectedServices.size / 4f).coerceIn(0.25f, 1f)
                LinearProgressIndicator(
                    progress = { tierProgress },
                    color = GroomlyElectricLime,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
                Text(
                    text = if (breakdown.selfPackageDiscountPercent > 0) {
                        "🎉 Self-Created Package Tier Active: ${selectedServices.size} Services = ${breakdown.selfPackageDiscountPercent}% Automatic Bundle Discount (-₹${breakdown.selfPackageDiscountInr})"
                    } else {
                        "Add 1 more service to your Haircut to unlock 10% Self-Created Package Discount!"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = GroomlySuccessMint,
                    fontWeight = FontWeight.Bold
                )
            }
        }

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
                    .animateContentSize()
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
                                        text = "CURATED BUNDLE",
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
                        Text(
                            text = "${service.durationMinutes} mins (+${service.bufferMinutes}m prep buffer)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
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
                    label = { Text(if (isAvailable) slot else "$slot (Booked)") }
                )
            }
        }

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
                            "Professional arrives at your GPS address with sanitized kit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(checked = bookAsHomeService, onCheckedChange = { bookAsHomeService = it })
            }
        }

        // STEP 4: Custom Pre-Charge & Payment Gateway Options
        Text(
            text = "4. Pre-Charge According to Your Wishes & Payment Gateway",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = couponInput,
            onValueChange = { couponInput = it.uppercase() },
            label = { Text("Promo Code (BYOP18, GROOM100, LIME20, BRIDALGLOW)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("ADVANCE", 20, "20% Pre-Charge Lock"),
                Triple("HALF", 50, "50% Half Pre-Charge"),
                Triple("FULL", 100, "100% Full Pre-Pay"),
                Triple("PAY_AT_VENUE", 0, "Cash at Salon")
            ).forEach { (typeKey, pct, label) ->
                FilterChip(
                    selected = selectedPaymentType == typeKey && prechargePercent == pct,
                    onClick = {
                        selectedPaymentType = typeKey
                        prechargePercent = pct
                    },
                    label = { Text(label) }
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "UPI" to "⚡ UPI App (GPay/PhonePe)",
                "CARD" to "🛡️ Razorpay / Stripe Card",
                "NET_BANKING" to "🏦 NetBanking",
                "WALLET" to "👛 Groomly Wallet",
                "CASH_AT_SHOP" to "💵 Cash at Shop"
            ).forEach { (method, label) ->
                FilterChip(
                    selected = selectedPaymentMethod == method,
                    onClick = { selectedPaymentMethod = method },
                    label = { Text(label) }
                )
            }
        }

        // Transparent Self-Created Package & Pre-Charge Breakdown Card
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
                    text = "SELF-CREATED PACKAGE & PRE-CHARGE RECEIPT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                BreakdownRow("Selected Services (${selectedServices.size} items)", "₹${breakdown.subtotalInr}")
                BreakdownRow("Total Duration (incl. buffer)", "${breakdown.totalDurationMinutes} mins")
                if (breakdown.selfPackageDiscountInr > 0) {
                    BreakdownRow(
                        "Self-Created Package Bonus (${breakdown.selfPackageDiscountPercent}%)",
                        "- ₹${breakdown.selfPackageDiscountInr}",
                        highlight = true
                    )
                }
                if (breakdown.couponDiscountInr > 0) {
                    BreakdownRow("Coupon Discount (${breakdown.appliedCoupon})", "- ₹${breakdown.couponDiscountInr}")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                BreakdownRow("Net Custom Package Total", "₹${breakdown.netTotalInr}", bold = true)
                BreakdownRow(
                    "Pre-Charged Now ($selectedPaymentMethod)",
                    "₹${breakdown.advancePayableInr}",
                    highlight = true
                )
                BreakdownRow("Remaining Balance at Salon", "₹${breakdown.remainingBalanceInr}", bold = true)
            }
        }

        // Action Buttons: Gateway Checkout + Direct UPI + WhatsApp Share
        Button(
            onClick = {
                if (selectedPaymentType == "PAY_AT_VENUE" || breakdown.advancePayableInr == 0) {
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
                        paymentType = "PAY_AT_VENUE",
                        prechargeCustomPercent = 0,
                        isHomeService = bookAsHomeService,
                        onComplete = onBack
                    )
                } else {
                    showGatewayModal = true
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("confirm_smart_booking_button")
        ) {
            Icon(Icons.Default.Payment, contentDescription = "Pay", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pre-Charge ₹${breakdown.advancePayableInr} & Confirm Package (Balance ₹${breakdown.remainingBalanceInr})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiGatewayPaymentDialog(
    amountInr: Int,
    initialGateway: String,
    serviceDescription: String,
    onDismiss: () -> Unit,
    onLaunchUpiApp: () -> Unit,
    onPaymentAuthorized: (String) -> Unit
) {
    var chosenMethod by remember { mutableStateOf(initialGateway) }
    var upiIdInput by remember { mutableStateOf("customer@okicici") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "UPI & Multi-Gateway Pre-Charge • ₹$amountInr",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = serviceDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "UPI" to "UPI / QR",
                        "CARD" to "Razorpay / Stripe",
                        "NET_BANKING" to "NetBanking",
                        "WALLET" to "Groomly Wallet"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = chosenMethod == key,
                            onClick = { chosenMethod = key },
                            label = { Text(label) }
                        )
                    }
                }

                if (chosenMethod == "UPI") {
                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it },
                        label = { Text("Enter VPA / UPI ID (e.g. name@okaxis)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = onLaunchUpiApp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📲 Open Installed UPI App (GPay / PhonePe / Paytm)")
                    }
                } else {
                    Text(
                        text = "256-Bit Encrypted Gateway Checkout via Razorpay & Stripe International.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GroomlySuccessMint
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPaymentAuthorized(chosenMethod) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GroomlyElectricLime,
                    contentColor = GroomlyObsidian
                )
            ) {
                Text("Authorize ₹$amountInr & Book", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
