package com.pocketnest.ssotest

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import org.json.JSONObject
import android.os.Message

private const val REDIRECT_SCHEME = "pocketnesthostedlink"
// Swap to prod for release builds
private const val BASE_URL = "http://192.168.0.236:8081/?redirect_uri=$REDIRECT_SCHEME"

class WebViewActivity : ComponentActivity() {

    private lateinit var webView: WebView


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        // Safe area / insets (optional)
        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = sys.left, top = sys.top, right = sys.right, bottom = sys.bottom)
            insets
        }

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.setSupportMultipleWindows(true)

        // Inject the same bridge function used on iOS
        val bridgeJS = """
            (function(){
              window.HostBridge = window.HostBridge || {};
              window.HostBridge.onHostedLinkComplete = function (payload) {
                try {
                  var data = (typeof payload === 'string') ? JSON.parse(payload) : payload;
                  window.dispatchEvent(new CustomEvent('hosted-link-complete', { detail: data }));
                } catch (e) { console.error('HostBridge payload parse error', e); }
              };
              // Provide a consistent "native" bridge API
              // On iOS: window.webkit.messageHandlers.native.postMessage(...)
              // On Android: window.native.postMessage(...)
              if (!window.webkit) window.webkit = { messageHandlers: {} };
              if (!window.webkit.messageHandlers) window.webkit.messageHandlers = {};
              if (!window.native) {
                window.native = {
                  postMessage: function (msg) {
                    try { NativeBridge.postMessage(typeof msg === 'string' ? msg : JSON.stringify(msg)); }
                    catch (e) { console.error('NativeBridge error', e); }
                  }
                };
              }
              if (!window.webkit.messageHandlers.native) {
                window.webkit.messageHandlers.native = { postMessage: window.native.postMessage };
              }
            })();
        """.trimIndent()

        webView.addJavascriptInterface(NativeBridge { json ->
            handleWebMessage(json)
        }, "NativeBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                val isExternal = !url.startsWith(BASE_URL)

                return if (isExternal) {
                    // Open any external (non-Plaid CDN) links in external browser
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) { }
                    true
                } else {
                    false // allow WebView to load
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // Inject bridge early (also injected at doc start below via UserScript-like approach)
                view.evaluateJavascript(bridgeJS, null)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            // Handle window.open / target=_blank (load into same WebView)
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = webView
                resultMsg?.sendToTarget()
                return true
            }
        }

        // Inject the bridge at document start
        webView.evaluateJavascript("(function(){ $bridgeJS })();", null)

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(BASE_URL)
        }

        // If activity started via deep link while app was closed
        intent?.data?.let { handleDeepLink(it) }
    }

    private fun handleWebMessage(json: String) {
        Log.d("WebViewActivity", "MESSAGE $json")
        try {
            val obj = JSONObject(json)
            val type = obj.optString("type", "")
            if (type == "openHostedLink") {
                val url = obj.optString("url", "")
                if (url.isNotEmpty()) openHostedLink(url)
            }
        } catch (e: Exception) {
            Log.e("WebViewActivity", "Failed to parse message: $json", e)
        }
    }

    private fun openHostedLink(hostedLinkUrl: String) {
        // Chrome Custom Tabs
        val uri = Uri.parse(hostedLinkUrl)
        val intent = CustomTabsIntent.Builder()
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setShowTitle(true)
            .build()

        try {
            intent.launchUrl(this, uri)
        } catch (e: Exception) {
            // Fallback to external browser if no Custom Tabs
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    // Deep link arrives here when already running (singleTop)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.data?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(uri: Uri) {
        Log.v("WebViewActivity", "Verbose log example")
        Log.d("WebViewActivity", "Received from web: $uri")
        println(uri)
        if (uri.scheme != REDIRECT_SCHEME) return

        // Convert query params to map
        val params = mutableMapOf<String, String>()
        uri.queryParameterNames.forEach { name ->
            params[name] = uri.getQueryParameter(name) ?: ""
        }

        val payload = JSONObject().apply {
            put("status", "success") // Treat all arrivals as success; see note below about "cancel"
            put("callbackURL", uri.toString())
            put("params", JSONObject(params as Map<*, *>))
        }.toString()

        notifyWeb(payload)
    }

    private fun notifyWeb(jsonPayload: String) {
        // Call the same bridge the iOS code uses
        val js = "window.HostBridge.onHostedLinkComplete($jsonPayload)"
        runOnUiThread {
            webView.evaluateJavascript(js, null)
        }
    }
}

/** JS interface for window.native.postMessage(...) */
class NativeBridge(private val onMessage: (String) -> Unit) {
    @JavascriptInterface
    fun postMessage(json: String) = onMessage(json)
}