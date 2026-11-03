package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.integration.PaymentAndWhatsAppService
import com.example.data.model.GroomlyMarketplaceCatalog
import com.example.ui.viewmodel.GroomlyViewModel

private class GroomlyHtmlBridge(
    private val onRequestGps: () -> Unit,
    private val onCitySelected: (String, Double, Double) -> Unit,
    private val onBookCustomPackage: (String, String, String, Int, Int, Int, Int, String) -> Unit,
    private val onLaunchUpi: (Int, String) -> Unit,
    private val onSendWhatsApp: (String, String) -> Unit,
    private val onOpenProvider: (String) -> Unit,
    private val onJoinQueue: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun requestLiveGpsLocation() {
        mainHandler.post { onRequestGps() }
    }

    @JavascriptInterface
    fun onCitySelected(cityLabel: String, lat: Double, lng: Double) {
        mainHandler.post { onCitySelected(cityLabel, lat, lng) }
    }

    @JavascriptInterface
    fun bookCustomPackageFromHtml(
        providerId: String,
        providerName: String,
        serviceSummary: String,
        durationMins: Int,
        netTotalInr: Int,
        prechargedAdvanceInr: Int,
        remainingBalanceInr: Int,
        gateway: String
    ) {
        mainHandler.post {
            onBookCustomPackage(
                providerId,
                providerName,
                serviceSummary,
                durationMins,
                netTotalInr,
                prechargedAdvanceInr,
                remainingBalanceInr,
                gateway
            )
        }
    }

    @JavascriptInterface
    fun launchUpiFromHtml(amountInr: Int, summary: String) {
        mainHandler.post { onLaunchUpi(amountInr, summary) }
    }

    @JavascriptInterface
    fun sendWhatsAppFromHtml(phone: String, message: String) {
        mainHandler.post { onSendWhatsApp(phone, message) }
    }

    @JavascriptInterface
    fun openProviderNative(providerId: String) {
        mainHandler.post { onOpenProvider(providerId) }
    }

    @JavascriptInterface
    fun joinQueueNative(providerId: String) {
        mainHandler.post { onJoinQueue(providerId) }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlMainPageScreen(
    viewModel: GroomlyViewModel,
    onOpenGatewayModal: (Int, String, String, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val coords = if (granted) {
            PaymentAndWhatsAppService.fetchDeviceGpsCoordinates(context) ?: (12.9784 to 77.6408)
        } else {
            12.9784 to 77.6408
        }
        val label = if (granted) "Live GPS • Indiranagar, Bengaluru" else "Bengaluru Hub (Permission Default)"
        viewModel.updateActiveGpsCoordinates(label, coords.first, coords.second)
        webViewRef?.evaluateJavascript(
            "updateGpsFromAndroid('$label', ${coords.first}, ${coords.second});",
            null
        )
    }

    val bridge = remember(viewModel) {
        GroomlyHtmlBridge(
            onRequestGps = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onCitySelected = { city, lat, lng ->
                viewModel.updateActiveGpsCoordinates(city, lat, lng)
            },
            onBookCustomPackage = { provId, provName, summary, mins, netTotal, advance, balance, gateway ->
                onOpenGatewayModal(advance, gateway, summary) {
                    viewModel.bookSelfCreatedCustomPackage(
                        providerId = provId,
                        providerName = provName,
                        serviceSummary = summary,
                        durationMinutes = mins,
                        netTotalInr = netTotal,
                        prechargedAdvanceInr = advance,
                        remainingBalanceInr = balance,
                        paymentGateway = gateway
                    )
                }
            },
            onLaunchUpi = { amount, note ->
                val launched = PaymentAndWhatsAppService.launchUpiPaymentIntent(
                    context = context,
                    amountInr = amount,
                    merchantName = "Groomly Marketplace",
                    transactionNote = note
                )
                if (!launched) {
                    onOpenGatewayModal(amount, "UPI", note) {}
                }
            },
            onSendWhatsApp = { phone, msg ->
                viewModel.dispatchWhatsAppAndOpenChat(context, phone, msg)
            },
            onOpenProvider = { providerId ->
                val found = GroomlyMarketplaceCatalog.providers.firstOrNull { it.id == providerId }
                    ?: GroomlyMarketplaceCatalog.providers.first()
                viewModel.selectProviderForDetail(found)
            },
            onJoinQueue = { providerId ->
                val found = GroomlyMarketplaceCatalog.providers.firstOrNull { it.id == providerId }
                    ?: GroomlyMarketplaceCatalog.providers.first()
                viewModel.joinProviderLiveQueue(
                    provider = found,
                    serviceName = found.services.first().name,
                    staffName = found.staff.first().name
                )
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .testTag("groomly_html_main_webview"),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                addJavascriptInterface(bridge, "GroomlyBridge")
                loadUrl("file:///android_asset/groomly_main.html")
                webViewRef = this
            }
        }
    )
}
