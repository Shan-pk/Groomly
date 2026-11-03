package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.AppRoleMode
import com.example.data.repository.GroomlyRepository
import com.example.ui.auth.GroomlyAuthScreen
import com.example.ui.auth.signOutGroomly
import com.example.ui.screens.ArchitectureBlueprintScreen
import com.example.ui.screens.CustomerEcosystemScreen
import com.example.ui.screens.ProviderStudioScreen
import com.example.ui.screens.SuperAdminPanelScreen
import com.example.ui.theme.GroomlyElectricLime
import com.example.ui.theme.GroomlyObsidian
import com.example.ui.theme.GroomlySuccessMint
import com.example.ui.theme.GroomlyTheme
import com.example.ui.viewmodel.GroomlyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroomlyRootApp()
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun GroomlyRootApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        GroomlyTheme(darkTheme = true) {
            GroomlyAuthScreen(onAuthSuccess = {})
        }
    } else {
        val viewModel: GroomlyViewModel = viewModel(
            key = user.uid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY]) {
                        "APPLICATION_KEY missing from CreationExtras"
                    }
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    GroomlyViewModel(
                        repository = GroomlyRepository(db),
                        currentUserId = user.uid,
                        userDisplayName = user.displayName ?: "Aarav Sharma"
                    )
                }
            }
        )
        val isDark by viewModel.isDarkTheme.collectAsState()
        GroomlyTheme(darkTheme = isDark) {
            GroomlyEcosystemShell(viewModel = viewModel)
        }
    }
}

@Composable
fun GroomlyEcosystemShell(viewModel: GroomlyViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeRoleMode by viewModel.activeRoleMode.collectAsState()
    val customerTab by viewModel.customerTabIndex.collectAsState()
    val providerTab by viewModel.providerTabIndex.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val statusBanner by viewModel.statusBannerMessage.collectAsState()

    BackHandler(enabled = activeRoleMode != AppRoleMode.CUSTOMER || customerTab != 0) {
        if (activeRoleMode != AppRoleMode.CUSTOMER) {
            viewModel.setRoleMode(AppRoleMode.CUSTOMER)
        } else {
            viewModel.setCustomerTab(0)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GroomlyElectricLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = "Groomly",
                                tint = GroomlyObsidian,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GROOMLY",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeRoleMode.badge,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark or Light Theme"
                        )
                    }
                }

                // Ecosystem Role Switcher Bar (Customer App | Provider Studio | Super Admin | Blueprint)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppRoleMode.entries.forEach { mode ->
                        val selected = activeRoleMode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .clickable { viewModel.setRoleMode(mode) }
                                .testTag("role_mode_${mode.name.lowercase()}")
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = statusBanner != null) {
                    Surface(
                        color = GroomlySuccessMint.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { viewModel.clearStatusBanner() }
                    ) {
                        Text(
                            text = statusBanner ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            color = GroomlySuccessMint,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            when (activeRoleMode) {
                AppRoleMode.CUSTOMER -> {
                    NavigationBar {
                        val items = listOf(
                            Triple("Home", Icons.Default.Home, "nav_customer_home"),
                            Triple("Explore", Icons.Default.Explore, "nav_customer_explore"),
                            Triple("Bookings", Icons.Default.CalendarMonth, "nav_customer_bookings"),
                            Triple("Favourites", Icons.Default.Favorite, "nav_customer_favourites"),
                            Triple("Profile", Icons.Default.Person, "nav_customer_profile")
                        )
                        items.forEachIndexed { idx, (label, icon, tag) ->
                            NavigationBarItem(
                                selected = customerTab == idx,
                                onClick = {
                                    viewModel.selectProviderForDetail(null)
                                    viewModel.setCustomerTab(idx)
                                },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                modifier = Modifier.testTag(tag)
                            )
                        }
                    }
                }
                AppRoleMode.PROVIDER -> {
                    NavigationBar {
                        val items = listOf(
                            Triple("Dashboard", Icons.Default.Dashboard, "nav_provider_dashboard"),
                            Triple("Calendar", Icons.Default.CalendarMonth, "nav_provider_calendar"),
                            Triple("Queue", Icons.Default.QrCodeScanner, "nav_provider_queue"),
                            Triple("Customers", Icons.Default.Groups, "nav_provider_customers"),
                            Triple("Business", Icons.Default.Storefront, "nav_provider_business")
                        )
                        items.forEachIndexed { idx, (label, icon, tag) ->
                            NavigationBarItem(
                                selected = providerTab == idx,
                                onClick = { viewModel.setProviderTab(idx) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                modifier = Modifier.testTag(tag)
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeRoleMode) {
                AppRoleMode.CUSTOMER -> CustomerEcosystemScreen(
                    viewModel = viewModel,
                    onSignOut = {
                        signOutGroomly(context = context, onSignOutComplete = {}, scope = scope)
                    }
                )
                AppRoleMode.PROVIDER -> ProviderStudioScreen(viewModel = viewModel)
                AppRoleMode.ADMIN -> SuperAdminPanelScreen(viewModel = viewModel)
                AppRoleMode.ARCHITECTURE -> ArchitectureBlueprintScreen()
            }
        }
    }
}
