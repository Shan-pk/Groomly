package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.GroomlyMarketplaceCatalog
import com.example.data.model.ProviderBusiness
import com.example.data.model.QueueTicket
import com.example.data.model.Review
import com.example.ui.theme.GroomlyObsidian
import com.example.ui.theme.GroomlySuccessMint
import com.example.ui.theme.GroomlyWarningAmber
import com.example.ui.viewmodel.GroomlyViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProviderStudioScreen(viewModel: GroomlyViewModel) {
    val tabIndex by viewModel.providerTabIndex.collectAsState()
    val appointmentsState by viewModel.appointmentsState.collectAsState()
    val queueState by viewModel.queueTicketsState.collectAsState()
    val businessesState by viewModel.providerBusinessesState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()

    val appointments = (appointmentsState as? UiState.Success)?.data.orEmpty()
    val queueTickets = (queueState as? UiState.Success)?.data.orEmpty()
    val businesses = (businessesState as? UiState.Success)?.data.orEmpty()
    val reviews = (reviewsState as? UiState.Success)?.data.orEmpty()

    var walkInCustomerService by remember { mutableStateOf("Express Skin Fade & Wash") }
    var newBizName by remember { mutableStateOf("") }
    var newBizCategory by remember { mutableStateOf("Unisex Luxury Salon") }
    var newBizVertical by remember { mutableStateOf("SALON") }

    val grossRevenueInr = appointments.filter { it.status !in listOf("CANCELLED", "REFUNDED") }.sumOf { it.totalAmountInr }
    val commissionRate = businesses.firstOrNull()?.commissionRatePercent ?: 15L
    val platformCommissionInr = (grossRevenueInr * commissionRate) / 100L
    val gatewayFeesInr = (grossRevenueInr * 2) / 100L
    val netPayoutInr = (grossRevenueInr - platformCommissionInr - gatewayFeesInr).coerceAtLeast(0L)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (tabIndex) {
            // 0: PROVIDER DASHBOARD
            0 -> {
                item {
                    Text(
                        text = "Provider Business Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = businesses.firstOrNull()?.businessName ?: "Royal Gents Flagship Studio",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiMetricCard(
                            title = "Total Bookings",
                            value = "${appointments.size}",
                            subtitle = "${appointments.count { it.status == "CONFIRMED" }} active",
                            modifier = Modifier.weight(1f)
                        )
                        KpiMetricCard(
                            title = "Gross Revenue",
                            value = "₹$grossRevenueInr",
                            subtitle = "Net ₹$netPayoutInr",
                            modifier = Modifier.weight(1f)
                        )
                        KpiMetricCard(
                            title = "Live Queue",
                            value = "${queueTickets.count { it.status !in listOf("COMPLETED", "CANCELLED") }}",
                            subtitle = "Active Tokens",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text(
                        text = "Staff Performance & Schedules (Royal Gents Roster)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(GroomlyMarketplaceCatalog.providers.first().staff, key = { it.id }) { staff ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${staff.name} – ${staff.roleTitle}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Specialty: ${staff.specialty}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Shift: ${staff.shiftHours} • Break: ${staff.breakWindow}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "★ ${staff.rating}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GroomlySuccessMint,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${staff.completedJobs} jobs",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            // 1: CALENDAR & SLOT / BOOKING MANAGEMENT
            1 -> {
                item {
                    Text(
                        text = "Calendar, Slot & Walk-In Manager",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Quick Walk-In Appointment Registration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = walkInCustomerService,
                                onValueChange = { walkInCustomerService = it },
                                label = { Text("Walk-In Service Package") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    val prov = GroomlyMarketplaceCatalog.providers.first()
                                    viewModel.confirmSmartAppointment(
                                        provider = prov,
                                        selectedServices = listOf(prov.services.first()),
                                        staffId = "staff_ahmed",
                                        staffName = "Ahmed – Senior Barber",
                                        staffAddonInr = 0,
                                        dateIso = "2026-11-04",
                                        timeSlot = "03:00 PM",
                                        couponCode = "NONE",
                                        paymentMethod = "CASH_AT_SHOP",
                                        paymentType = "PAY_AT_VENUE",
                                        isHomeService = false,
                                        onComplete = {}
                                    )
                                },
                                modifier = Modifier.testTag("register_walkin_button")
                            ) {
                                Text("Add Walk-In Slot to Calendar")
                            }
                        }
                    }
                }

                items(appointments, key = { it.id }) { appt ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${appt.dateIso} • ${appt.timeSlot} (${appt.durationMinutes}m)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = appt.status,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GroomlySuccessMint
                                )
                            }
                            Text(
                                text = "${appt.serviceSummary} • Staff: ${appt.staffName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total ₹${appt.totalAmountInr} (Advance ₹${appt.advancePaidInr}, Balance ₹${appt.remainingBalanceInr})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (appt.status !in listOf("CANCELLED", "REFUNDED", "COMPLETED")) {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { viewModel.updateAppointmentLifecycle(appt, "ARRIVED") }) {
                                        Text("Arrived")
                                    }
                                    OutlinedButton(onClick = { viewModel.updateAppointmentLifecycle(appt, "IN_PROGRESS") }) {
                                        Text("Start")
                                    }
                                    Button(onClick = { viewModel.updateAppointmentLifecycle(appt, "COMPLETED") }) {
                                        Text("Complete & Settle")
                                    }
                                    TextButton(onClick = { viewModel.updateAppointmentLifecycle(appt, "REFUNDED") }) {
                                        Text("Reject & Refund", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2: LIVE QUEUE CONTROLLER
            2 -> {
                item {
                    Text(
                        text = "Real-Time Queue & QR Check-In Desk",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Advance customer tokens and verify QR check-in codes in real time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(queueTickets, key = { it.id }) { ticket ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Token #${ticket.queueNumber} • ${ticket.status} (QR: ${ticket.qrCheckInCode})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${ticket.serviceName} • Assigned: ${ticket.staffName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Currently Serving #${ticket.currentlyServingNumber} • Est. Wait ~${ticket.estimatedWaitMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = GroomlySuccessMint
                            )
                            if (ticket.status !in listOf("COMPLETED", "CANCELLED")) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.advanceOrCheckInQueueTicket(ticket, "ADVANCE_TURN") }
                                    ) {
                                        Text("Call Next Turn")
                                    }
                                    Button(
                                        onClick = { viewModel.advanceOrCheckInQueueTicket(ticket, "COMPLETE") }
                                    ) {
                                        Text("Complete Service")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3: CUSTOMERS & VERIFIED REVIEWS
            3 -> {
                item {
                    Text(
                        text = "Customer CRM & Verified Reviews (${reviews.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(reviews, key = { it.id }) { rev ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "${rev.providerName} • ★ ${rev.rating}/5 (Staff ${rev.staffName}: ★ ${rev.staffRating}/5)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = GroomlySuccessMint
                            )
                            Text(text = rev.comment, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { viewModel.toggleReportReview(rev) }) {
                                Text(if (rev.isReported) "Reported to Admin (Undo)" else "Report Inappropriate Review")
                            }
                        }
                    }
                }
            }

            // 4: BUSINESS FINANCE & REGISTRATION
            4 -> {
                item {
                    Text(
                        text = "Business Finance, Settlements & Onboarding",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
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
                                text = "PROVIDER SETTLEMENT & COMMISSION LEDGER",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Gross Booking Revenue: ₹$grossRevenueInr", style = MaterialTheme.typography.bodyMedium)
                            Text("Platform Commission ($commissionRate%): - ₹$platformCommissionInr", style = MaterialTheme.typography.bodyMedium)
                            Text("Razorpay / UPI Gateway Fees (2%): - ₹$gatewayFeesInr", style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "Net Provider Earnings (Ready for Payout): ₹$netPayoutInr",
                                style = MaterialTheme.typography.titleMedium,
                                color = GroomlySuccessMint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Register Additional Multi-Vendor Branch / Clinic",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = newBizName,
                                onValueChange = { newBizName = it },
                                label = { Text("Business / Clinic Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newBizCategory,
                                onValueChange = { newBizCategory = it },
                                label = { Text("Primary Category (e.g. Bridal Studio, Skin Clinic)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("SALON", "BRIDAL", "DERMATOLOGY", "DIETITIAN").forEach { vert ->
                                    FilterChip(
                                        selected = newBizVertical == vert,
                                        onClick = { newBizVertical = vert },
                                        label = { Text(vert) }
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    if (newBizName.isNotBlank()) {
                                        viewModel.registerNewProviderBusiness(
                                            ownerName = viewModel.userDisplayName.ifBlank { "Studio Owner" },
                                            businessName = newBizName,
                                            category = newBizCategory,
                                            vertical = newBizVertical,
                                            genderFocus = "UNISEX",
                                            cityArea = "Indiranagar, Bengaluru",
                                            openingHours = "09:30 AM - 09:00 PM",
                                            homeServiceEnabled = true
                                        )
                                        newBizName = ""
                                    }
                                },
                                modifier = Modifier.testTag("register_provider_business_button")
                            ) {
                                Text("Submit for Admin Verification")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuperAdminPanelScreen(viewModel: GroomlyViewModel) {
    val businessesState by viewModel.providerBusinessesState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val whatsappLogs by viewModel.whatsappLogs.collectAsState()
    val defaultCommission by viewModel.defaultPlatformCommission.collectAsState()
    val monthlySaaSInr by viewModel.monthlySaaSSubscriptionInr.collectAsState()

    val businesses = (businessesState as? UiState.Success)?.data.orEmpty()
    var sliderCommission by remember(defaultCommission) { mutableFloatStateOf(defaultCommission.toFloat()) }
    var newCategoryName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Super Admin Marketplace Governance (25 Modules)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Centralized control plane for Provider Verification, Dynamic Commissions, Categories, Subscriptions & Audit Logs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Dynamic Commission & Monetization Engine
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CONFIGURABLE MONETIZATION & COMMISSION ENGINE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Platform Commission per Completed Booking: ${sliderCommission.toInt()}% • Provider SaaS: ₹$monthlySaaSInr/mo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = sliderCommission,
                        onValueChange = { sliderCommission = it },
                        valueRange = 5f..40f,
                        onValueChangeFinished = {
                            viewModel.updateAdminMonetizationConfig(sliderCommission.toInt(), monthlySaaSInr)
                        }
                    )
                }
            }
        }

        // Provider Verification & Suspension Queue
        item {
            Text(
                text = "Provider Verification & Governance (${businesses.size} Registered)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(businesses, key = { it.id }) { biz ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${biz.businessName} (${biz.vertical})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = biz.verificationStatus,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (biz.verificationStatus == "VERIFIED") GroomlySuccessMint else GroomlyWarningAmber
                        )
                    }
                    Text(
                        text = "Owner: ${biz.ownerName} • ${biz.cityArea} • Commission: ${biz.commissionRatePercent}% • Plan: ${biz.subscriptionTier}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.updateProviderBusinessGovernance(biz, newVerificationStatus = "VERIFIED")
                            }
                        ) {
                            Text("Verify Business")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.updateProviderBusinessGovernance(biz, newVerificationStatus = "SUSPENDED")
                            }
                        ) {
                            Text("Suspend")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.updateProviderBusinessGovernance(
                                    biz,
                                    newCommissionPercent = sliderCommission.toInt()
                                )
                            }
                        ) {
                            Text("Apply ${sliderCommission.toInt()}% Comm.")
                        }
                    }
                }
            }
        }

        // Dynamic Category Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dynamic Service Category Manager (${categories.size} Active)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Add Marketplace Category (e.g. Hydra-Laser)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    viewModel.addMarketplaceCategory(newCategoryName, "SALON", "UNISEX", 599)
                                    newCategoryName = ""
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }

        // Official WhatsApp Business API & Audit Logs
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "WHATSAPP BUSINESS PLATFORM & AUDIT LOGS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    whatsappLogs.forEach { entry ->
                        Text(text = "• $entry", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ArchitectureBlueprintScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "GROOMLY System Architecture & Roadmap",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Complete Multi-Vendor SaaS Blueprint, 26-Entity Relational & Cloud Schema, and Phase 1–3 Expansion Roadmap.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(GroomlyMarketplaceCatalog.architectureDeliverables, key = { it.number }) { item ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PILLAR 0${item.number} • ${item.category.uppercase()}",
                            color = GroomlyObsidian,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    item.keyHighlights.forEach { pt ->
                        Text(
                            text = "✓ $pt",
                            style = MaterialTheme.typography.bodySmall,
                            color = GroomlySuccessMint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
