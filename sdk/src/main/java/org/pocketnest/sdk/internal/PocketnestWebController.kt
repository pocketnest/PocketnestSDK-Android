// file: org/pocketnest/sdk/internal/PocketnestWebController.kt
package org.pocketnest.sdk.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.webkit.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import org.json.JSONObject

internal class PocketnestWebController(
    private val context: Context,
    private val startUrl: String,
    private val redirectUri: String?,
    private val accessToken: String?,
    private val onPresented: () -> Unit,
    private val onClosed: () -> Unit
) {
    lateinit var webView: WebView
        private set

    // ---- Custom Tabs resume support ----
    private var lastHostedLinkUrl: String? = null

    // Prevent opening the tab multiple times / loops
    private var lastTabLaunchUptimeMs: Long = 0L
    private val TAB_LAUNCH_DEBOUNCE_MS = 1200L

    fun createWebView(savedInstanceState: Bundle?): WebView {
        webView = WebView(context)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            setSupportMultipleWindows(false)
        }

        webView.addJavascriptInterface(NativeBridge { handleWebMessage(it) }, "NativeBridge")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?
            ): Boolean {
                (resultMsg?.obj as? WebView.WebViewTransport)?.webView = webView
                resultMsg?.sendToTarget()
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, req: WebResourceRequest): Boolean {
                val target = req.url
                if (WebCore.isExternal(target, startUrl)) {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, target)) } catch (_: Exception) {}
                    return true
                }
                return false
            }

            override fun onPageFinished(v: WebView, url: String) {
                super.onPageFinished(v, url)
                v.evaluateJavascript(WebCore.BRIDGE_JS, null)
            }
        }

        webView.evaluateJavascript("(function(){ ${WebCore.BRIDGE_JS} })();", null)

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(WebCore.buildUrl(startUrl, redirectUri, accessToken))
        }

        onPresented()
        return webView
    }

    fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
    }

    fun onNewIntent(intent: Intent) {
        intent.data?.let(::handleDeepLink)
    }

    fun destroy() {
        try { webView.destroy() } catch (_: Exception) {}
        onClosed()
    }

    // ----- JS <-> Native -----

    private fun handleWebMessage(json: String) {
        try {
            val obj = JSONObject(json)
            when (obj.optString("type", "")) {
                "openHostedLink" ->
                    obj.optString("url", "").takeIf { it.isNotEmpty() }?.let(::openHostedLink)
                "onSuccess" -> {}
                "onExit" -> {}
            }
        } catch (e: Exception) {
            Log.e("PocketnestSDK", "Failed to parse message: $json", e)
        }
    }

    private fun openHostedLink(hostedLinkUrl: String) {
        lastHostedLinkUrl = hostedLinkUrl
        launchCustomTab(hostedLinkUrl)
    }

    private fun launchCustomTab(url: String) {
        // Debounce to prevent loops / double launches
        val now = SystemClock.uptimeMillis()
        if (now - lastTabLaunchUptimeMs < TAB_LAUNCH_DEBOUNCE_MS) return
        lastTabLaunchUptimeMs = now

        val uri = url.toUri()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .build()

        try {
            customTabsIntent.launchUrl(context, uri)
        } catch (_: Exception) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, uri)
                )
            } catch (_: Exception) {}
        }
    }

    // Host (or our Activity) forwards deep link here
    fun handleDeepLink(uri: Uri) {
        val expectedScheme = redirectUri?.trim() ?: return
        if (!expectedScheme.equals(uri.scheme, true)) return

        val params = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) ?: "" }
        val payload = JSONObject().apply {
            put("status", "success")
            put("callbackURL", uri.toString())
            put("params", JSONObject(params))
        }.toString()

        val js = "window.HostBridge.onHostedLinkComplete($payload)"
        webView.post { webView.evaluateJavascript(js, null) }
    }
}