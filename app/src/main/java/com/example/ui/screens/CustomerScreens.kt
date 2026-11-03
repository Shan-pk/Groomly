package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.GroomlyMarketplaceCatalog
import com.example.data.model.MarketplaceProvider
import com.example.data.model.QueueTicket
import com.example.data.model.Review
import com.example.data.model.UserProfile
import com.example.ui.theme.GroomlyElectricLime
import com.example.ui.theme.GroomlyMedicalCyan
import com.example.ui.theme.GroomlyObsidian
import com.example.ui.theme.GroomlySuccessMint
import com.example.ui.theme.GroomlyVibrantViolet
import com.example.ui.theme.GroomlyWarmIvory
import com.example.ui.viewmodel.GroomlyViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun CustomerEcosystemScreen(
    viewModel: GroomlyViewModel,
    onSignOut: () -> Unit
) {
    val tabIndex by viewModel.customerTabIndex.collectAsState()
    val selectedProvider by viewModel.selectedProviderForDetail.collectAsState()

    val profileState by viewModel.userProfileState.collectAsState()
    val appointmentsState by viewModel.appointmentsState.collectAsState()
    val queueState by viewModel.queueTicketsState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()

    val profile = (profileState as? UiState.Success)?.data ?: UserProfile(displayName = viewModel.userDisplayName)
    val appointments = (appointmentsState as? UiState.Success)?.data.orEmpty()
    val queueTickets = (queueState as? UiState.Success)?.data.orEmpty()
    val reviews = (reviewsState as? UiState.Success)?.data.orEmpty()

    var activeQueueModalTicket by remember { mutableStateOf<QueueTicket?>(null) }

    activeQueueModalTicket?.let { ticket ->
        LiveQueueQrDialog(
            ticket = ticket,
            onDismiss = { activeQueueModalTicket = null },
            onCheckIn = {
                viewModel.advanceOrCheckInQueueTicket(ticket, "QR_CHECK_IN")
                activeQueueModalTicket = null
            },
            onAdvanceTurn = {
                viewModel.advanceOrCheckInQueueTicket(ticket, "ADVANCE_TURN")
                activeQueueModalTicket = null
            },
            onCancelTicket = {
                viewModel.advanceOrCheckInQueueTicket(ticket, "CANCEL")
                activeQueueModalTicket = null
            }
        )
    }

    if (selectedProvider != null) {
        ProviderDetailAndBookingScreen(
            provider = selectedProvider!!,
            existingAppointments = appointments,
            reviews = reviews,
            isFavourite = profile.favouriteProviderIds.contains(selectedProvider!!.id),
            onToggleFavourite = { viewModel.toggleFavouriteProvider(selectedProvider!!.id) },
            onBack = { viewModel.selectProviderForDetail(null) },
            viewModel = viewModel
        )
        return
    }

    when (tabIndex) {
        0 -> CustomerHomeTab(
            profile = profile,
            appointments = appointments,
            queueTickets = queueTickets,
            viewModel = viewModel,
            onOpenQueueQr = { activeQueueModalTicket = it }
        )
        1 -> CustomerExploreTab(
            profile = profile,
            viewModel = viewModel
        )
        2 -> CustomerBookingsTab(
            appointments = appointments,
            queueTickets = queueTickets,
            viewModel = viewModel,
            onOpenQueueQr = { activeQueueModalTicket = it }
        )
        3 -> CustomerFavouritesTab(
            profile = profile,
            viewModel = viewModel
        )
        4 -> CustomerProfileAndWalletTab(
            profile = profile,
            reviews = reviews,
            viewModel = viewModel,
            onSignOut = onSignOut
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomerHomeTab(
    profile: UserProfile,
    appointments: List<Appointment>,
    queueTickets: List<QueueTicket>,
    viewModel: GroomlyViewModel,
    onOpenQueueQr: (QueueTicket) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val coupons by viewModel.coupons.collectAsState()
    val activeQueue = queueTickets.firstOrNull { it.status !in listOf("COMPLETED", "CANCELLED") }
    val upcomingAppts = appointments.filter { it.status in listOf("CONFIRMED", "RESCHEDULED", "ARRIVED", "IN_PROGRESS") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location & Search Bar Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "CURRENT LOCATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = profile.cityLocation,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "₹${profile.walletBalanceInr} • ${profile.loyaltyPoints} Pts",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Search Trigger Bar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setCustomerTab(1) }
                    .testTag("home_search_bar")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "What service are you looking for? (Haircut, Bridal, Derma...)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Gen-Z Promotional Editorial Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF171A24), Color(0xFF281D46), Color(0xFF101318))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = GroomlyElectricLime,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "GEN-Z MULTI-VENDOR DROP • CODE: LIME20",
                                style = MaterialTheme.typography.labelMedium,
                                color = GroomlyObsidian,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Zero-Wait Grooming, Couture Bridal & Clinical Skin Care.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GroomlyWarmIvory,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lock your slot with 20% UPI Advance (e.g. ₹100 now, ₹400 at salon) or join a real-time digital walk-in queue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GroomlyWarmIvory.copy(alpha = 0.8f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    viewModel.selectProviderForDetail(GroomlyMarketplaceCatalog.providers.first())
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GroomlyElectricLime,
                                    contentColor = GroomlyObsidian
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Book Royal Gents", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.setCustomerTab(1) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Explore Map", color = GroomlyWarmIvory)
                            }
                        }
                    }
                }
            }
        }

        // Live Queue Ticket Active Widget
        if (activeQueue != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenQueueQr(activeQueue) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIVE QUEUE TICKET #${activeQueue.queueNumber} • ${activeQueue.status}",
                                style = MaterialTheme.typography.labelLarge,
                                color = GroomlySuccessMint,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeQueue.providerName} • ${activeQueue.serviceName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Currently Serving #${activeQueue.currentlyServingNumber} • Est. ~${activeQueue.estimatedWaitMinutes}m wait (non-guaranteed)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onOpenQueueQr(activeQueue) }) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "Open QR Code",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Appointment Quick Card
        if (upcomingAppts.isNotEmpty()) {
            val nextAppt = upcomingAppts.first()
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setCustomerTab(2) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "UPCOMING APPOINTMENT • ${nextAppt.status}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${nextAppt.dateIso} • ${nextAppt.timeSlot}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${nextAppt.providerName} — ${nextAppt.serviceSummary}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Specialist: ${nextAppt.staffName} • Paid ₹${nextAppt.advancePaidInr} | Balance ₹${nextAppt.remainingBalanceInr}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 16 Service Categories Grid
        item {
            Text(
                text = "16 Service Categories & Multi-Verticals",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            viewModel.setVerticalFilter(cat.vertical)
                            viewModel.setCustomerTab(1)
                        }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${cat.vertical} • From ₹${cat.startingPriceInr}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Exclusive Coupons Row
        item {
            Text(
                text = "Exclusive Offers & Coupons",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                coupons.forEach { coupon ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.width(240.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = coupon.code,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = coupon.title,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Min order ₹${coupon.minOrderInr} • Save up to ₹${coupon.maxDiscountInr}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Featured Multi-Vendor Providers
        item {
            Text(
                text = "Nearby Salons, Barbers, Bridal & Clinics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(GroomlyMarketplaceCatalog.providers, key = { it.id }) { provider ->
            ProviderMarketplaceCard(
                provider = provider,
                isFavourite = profile.favouriteProviderIds.contains(provider.id),
                onToggleFav = { viewModel.toggleFavouriteProvider(provider.id) },
                onClick = { viewModel.selectProviderForDetail(provider) },
                onJoinQueue = {
                    viewModel.joinProviderLiveQueue(
                        provider = provider,
                        serviceName = provider.services.first().name,
                        staffName = provider.staff.first().name
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomerExploreTab(
    profile: UserProfile,
    viewModel: GroomlyViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val genderFilter by viewModel.selectedGenderFilter.collectAsState()
    val verticalFilter by viewModel.selectedVerticalFilter.collectAsState()
    val onlyHomeService by viewModel.onlyHomeService.collectAsState()
    val onlyAvailableToday by viewModel.onlyAvailableToday.collectAsState()
    val isMapMode by viewModel.isMapMode.collectAsState()

    val filteredProviders = GroomlyMarketplaceCatalog.providers.filter { p ->
        val matchesQuery = searchQuery.isBlank() ||
            p.name.contains(searchQuery, ignoreCase = true) ||
            p.tagline.contains(searchQuery, ignoreCase = true) ||
            p.services.any { it.name.contains(searchQuery, ignoreCase = true) } ||
            p.staff.any { it.name.contains(searchQuery, ignoreCase = true) }
        val matchesGender = genderFilter == "ALL" || p.genderFocus == genderFilter
        val matchesVertical = verticalFilter == "ALL" || p.vertical == verticalFilter
        val matchesHome = !onlyHomeService || p.homeServiceAvailable
        val matchesToday = !onlyAvailableToday || p.isAvailableToday
        matchesQuery && matchesGender && matchesVertical && matchesHome && matchesToday
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explore & Advanced Discovery",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { viewModel.toggleMapMode() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("toggle_map_list_button")
                ) {
                    Icon(Icons.Default.Map, contentDescription = "Map Toggle", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isMapMode) "List View" else "Map View")
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search salon, barber (Ahmed), service, or clinic...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_search_input")
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "MENS", "WOMENS", "UNISEX").forEach { g ->
                    FilterChip(
                        selected = genderFilter == g,
                        onClick = { viewModel.setGenderFilter(g) },
                        label = { Text(g) }
                    )
                }
                listOf("ALL", "SALON", "BRIDAL", "DERMATOLOGY", "DIETITIAN").forEach { v ->
                    FilterChip(
                        selected = verticalFilter == v,
                        onClick = { viewModel.setVerticalFilter(v) },
                        label = { Text(v) }
                    )
                }
                FilterChip(
                    selected = onlyHomeService,
                    onClick = { viewModel.toggleHomeServiceFilter() },
                    label = { Text("Home Service") }
                )
                FilterChip(
                    selected = onlyAvailableToday,
                    onClick = { viewModel.toggleAvailableTodayFilter() },
                    label = { Text("Available Today") }
                )
            }
        }

        if (isMapMode) {
            item {
                InteractiveProvidersMapCard(
                    providers = filteredProviders,
                    onSelectProvider = { viewModel.selectProviderForDetail(it) }
                )
            }
        }

        items(filteredProviders, key = { it.id }) { provider ->
            ProviderMarketplaceCard(
                provider = provider,
                isFavourite = profile.favouriteProviderIds.contains(provider.id),
                onToggleFav = { viewModel.toggleFavouriteProvider(provider.id) },
                onClick = { viewModel.selectProviderForDetail(provider) },
                onJoinQueue = {
                    viewModel.joinProviderLiveQueue(
                        provider = provider,
                        serviceName = provider.services.first().name,
                        staffName = provider.staff.first().name
                    )
                }
            )
        }
    }
}

@Composable
private fun InteractiveProvidersMapCard(
    providers: List<MarketplaceProvider>,
    onSelectProvider: (MarketplaceProvider) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "POSTGIS LIVE RADAR MAP • BENGALURU CLUSTER (${providers.size} active pins)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF10141D))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Subtle grid lines
                    val step = size.width / 6f
                    for (i in 1..5) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.07f),
                            start = Offset(i * step, 0f),
                            end = Offset(i * step, size.height),
                            strokeWidth = 1.5f
                        )
                    }
                    // Draw radar circles
                    drawCircle(
                        color = GroomlyElectricLime.copy(alpha = 0.12f),
                        radius = size.minDimension * 0.42f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                    // Pins
                    val offsets = listOf(
                        Offset(0.28f, 0.35f),
                        Offset(0.66f, 0.28f),
                        Offset(0.48f, 0.62f),
                        Offset(0.75f, 0.72f),
                        Offset(0.22f, 0.74f)
                    )
                    providers.forEachIndexed { idx, prov ->
                        val pos = offsets[idx % offsets.size]
                        drawCircle(
                            color = Color(prov.accentHex),
                            radius = 16f,
                            center = Offset(size.width * pos.x, size.height * pos.y)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    providers.take(3).forEach { prov ->
                        Text(
                            text = "● ${prov.name} (${prov.distanceKm} km) • ★ ${prov.rating}",
                            style = MaterialTheme.typography.labelMedium,
                            color = GroomlyWarmIvory,
                            modifier = Modifier
                                .clickable { onSelectProvider(prov) }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderMarketplaceCard(
    provider: MarketplaceProvider,
    isFavourite: Boolean,
    onToggleFav: () -> Unit,
    onClick: () -> Unit,
    onJoinQueue: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("provider_card_${provider.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(provider.accentHex),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${provider.vertical} • ${provider.genderFocus}",
                            color = GroomlyObsidian,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (provider.isVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = GroomlySuccessMint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(onClick = onToggleFav) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favourite",
                        tint = if (isFavourite) Color(0xFFFB7185) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = provider.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = provider.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "📍 ${provider.address} (${provider.distanceKm} km) • ★ ${provider.rating} (${provider.reviewCount})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Specialists: " + provider.staff.joinToString(", ") { "${it.name} (${it.roleTitle})" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Queue #${provider.liveQueueCurrentServing} • From ₹${provider.services.minOf { it.discountPriceInr }}",
                        style = MaterialTheme.typography.labelLarge,
                        color = GroomlySuccessMint
                    )
                    Text(
                        text = if (provider.homeServiceAvailable) "Home Service & Walk-In Enabled" else "In-Studio & Walk-In Queue",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onJoinQueue,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Queue")
                    }
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Book Slot")
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerBookingsTab(
    appointments: List<Appointment>,
    queueTickets: List<QueueTicket>,
    viewModel: GroomlyViewModel,
    onOpenQueueQr: (QueueTicket) -> Unit
) {
    var filterTab by remember { mutableIntStateOf(0) } // 0 = Upcoming, 1 = Completed, 2 = Cancelled/Queue
    var reviewTargetAppt by remember { mutableStateOf<Appointment?>(null) }

    reviewTargetAppt?.let { appt ->
        WriteVerifiedReviewDialog(
            appointment = appt,
            onDismiss = { reviewTargetAppt = null },
            onSubmit = { rating, staffRating, comment, photoTag ->
                viewModel.submitVerifiedReview(appt, rating, staffRating, comment, photoTag)
                reviewTargetAppt = null
            }
        )
    }

    val filteredAppointments = when (filterTab) {
        0 -> appointments.filter { it.status in listOf("CONFIRMED", "RESCHEDULED", "ARRIVED", "IN_PROGRESS") }
        1 -> appointments.filter { it.status == "COMPLETED" }
        else -> appointments.filter { it.status in listOf("CANCELLED", "REFUNDED") }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "My Appointments & Live Queue",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterTab == 0,
                    onClick = { filterTab = 0 },
                    label = { Text("Upcoming") }
                )
                FilterChip(
                    selected = filterTab == 1,
                    onClick = { filterTab = 1 },
                    label = { Text("Completed") }
                )
                FilterChip(
                    selected = filterTab == 2,
                    onClick = { filterTab = 2 },
                    label = { Text("Cancelled & Refunds") }
                )
            }
        }

        if (queueTickets.isNotEmpty()) {
            item {
                Text(
                    text = "Live Queue Digital Tickets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(queueTickets, key = { it.id }) { ticket ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenQueueQr(ticket) }
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
                                text = "Token #${ticket.queueNumber} • ${ticket.providerName} (${ticket.status})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${ticket.serviceName} with ${ticket.staffName}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Serving #${ticket.currentlyServingNumber} • Est. ~${ticket.estimatedWaitMinutes}m (non-guaranteed)",
                                style = MaterialTheme.typography.labelMedium,
                                color = GroomlySuccessMint
                            )
                        }
                        Button(onClick = { onOpenQueueQr(ticket) }) {
                            Text("QR Pass")
                        }
                    }
                }
            }
        }

        if (filteredAppointments.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No appointments in this tab yet. Explore nearby salons to book in 30 seconds!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(filteredAppointments, key = { it.id }) { appt ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${appt.vertical} • ${appt.status}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${appt.dateIso} • ${appt.timeSlot}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = appt.providerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${appt.serviceSummary} (${appt.durationMinutes} mins)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Professional: ${appt.staffName} • Payment: ${appt.paymentMethod} (${appt.paymentType})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Total: ₹${appt.totalAmountInr} | Advance Paid: ₹${appt.advancePaidInr} | Balance: ₹${appt.remainingBalanceInr}",
                            style = MaterialTheme.typography.labelLarge,
                            color = GroomlySuccessMint
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            if (appt.status in listOf("CONFIRMED", "RESCHEDULED")) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.rescheduleExistingAppointment(appt, "2026-11-06", "04:30 PM")
                                    }
                                ) {
                                    Text("Reschedule")
                                }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateAppointmentLifecycle(appt, "COMPLETED")
                                    }
                                ) {
                                    Text("Mark Completed")
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.updateAppointmentLifecycle(appt, "CANCELLED")
                                    }
                                ) {
                                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                                }
                            } else if (appt.status == "COMPLETED") {
                                Button(onClick = { reviewTargetAppt = appt }) {
                                    Icon(Icons.Default.RateReview, contentDescription = "Review", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rate & Review Staff")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WriteVerifiedReviewDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onSubmit: (Int, Int, String, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var staffRating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("Loved the hygiene, punctuality, and styling precision!") }
    var photoTag by remember { mutableStateOf("After-Service Glow & Finish") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verified Review • ${appointment.providerName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Overall Rating: $rating ★ | Specialist (${appointment.staffName}): $staffRating ★")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { star ->
                        FilterChip(
                            selected = rating == star,
                            onClick = {
                                rating = star
                                staffRating = star
                            },
                            label = { Text("$star ★") }
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your Review") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = photoTag,
                    onValueChange = { photoTag = it },
                    label = { Text("Service Photo Caption") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, staffRating, comment, photoTag) }) {
                Text("Publish Verified Review")
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
private fun CustomerFavouritesTab(
    profile: UserProfile,
    viewModel: GroomlyViewModel
) {
    val favProviders = GroomlyMarketplaceCatalog.providers.filter {
        profile.favouriteProviderIds.contains(it.id)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Saved Salons & Specialists (${favProviders.size})",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(favProviders, key = { it.id }) { provider ->
            ProviderMarketplaceCard(
                provider = provider,
                isFavourite = true,
                onToggleFav = { viewModel.toggleFavouriteProvider(provider.id) },
                onClick = { viewModel.selectProviderForDetail(provider) },
                onJoinQueue = {
                    viewModel.joinProviderLiveQueue(
                        provider = provider,
                        serviceName = provider.services.first().name,
                        staffName = provider.staff.first().name
                    )
                }
            )
        }
    }
}

@Composable
private fun CustomerProfileAndWalletTab(
    profile: UserProfile,
    reviews: List<Review>,
    viewModel: GroomlyViewModel,
    onSignOut: () -> Unit
) {
    val whatsappLogs by viewModel.whatsappLogs.collectAsState()
    var newAddressText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = profile.displayName.ifBlank { "Groomly Member" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Location: ${profile.cityLocation} • Preference: ${profile.genderPreference}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Groomly Wallet: ₹${profile.walletBalanceInr}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Loyalty Points: ${profile.loyaltyPoints} Pts",
                                style = MaterialTheme.typography.labelLarge,
                                color = GroomlySuccessMint
                            )
                        }
                        Button(
                            onClick = { viewModel.topUpWalletAndPreferences(addWalletInr = 500L) },
                            modifier = Modifier.testTag("top_up_wallet_button")
                        ) {
                            Text("+ ₹500 UPI Top-Up")
                        }
                    }
                }
            }
        }

        // Saved Addresses
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Saved Home-Service Addresses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    profile.savedAddresses.forEach { addr ->
                        Text("• $addr", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newAddressText,
                            onValueChange = { newAddressText = it },
                            label = { Text("Add new address") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newAddressText.isNotBlank()) {
                                    viewModel.topUpWalletAndPreferences(newAddress = newAddressText)
                                    newAddressText = ""
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        // Notification & WhatsApp Business Consent
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WhatsApp Business API Alerts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Receive instant booking confirmations, reminders & live queue turn alerts", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = profile.whatsappAlertsEnabled,
                            onCheckedChange = { viewModel.topUpWalletAndPreferences(whatsappEnabled = it) }
                        )
                    }
                    Text(
                        text = "Recent WhatsApp Dispatches:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    whatsappLogs.take(3).forEach { log ->
                        Text(text = "• $log", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button")
            ) {
                Text("Sign Out of Google Account")
            }
        }
    }
}
