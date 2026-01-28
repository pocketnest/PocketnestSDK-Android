// file: org/pocketnest/sdk/WebViewActivity.kt
package org.pocketnest.sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import org.pocketnest.sdk.internal.PocketnestWebController
import org.pocketnest.sdk.internal.DeepLinkConsumer
import org.pocketnest.sdk.internal.DeepLinkRouter

class WebViewActivity : AppCompatActivity() {

    private lateinit var controller: PocketnestWebController
    private var deepLinkConsumer: DeepLinkConsumer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PocketnestSDK.notifyPresented()

        controller = PocketnestWebController(
            context = this,
            startUrl = Config.requireUrl(),
            redirectUri = Config.requireRedirectUrl(),
            accessToken = Config.requireAccessToken(),
            onPresented = { /* already notified */ },
            onClosed = { PocketnestSDK.notifyClosed() }
        )

        val webView = controller.createWebView(savedInstanceState)
        setContentView(webView)

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = sys.left,
                top = sys.top,
                right = sys.right,
                bottom = sys.bottom
            )
            insets
        }

        // IMPORTANT:
        // Do NOT call controller.handleDeepLink() here.
        // If activity was launched via deep link, route it instead.
        intent?.data?.let(DeepLinkRouter::route)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Route via router only
        intent.data?.let(DeepLinkRouter::route)
    }

    override fun onStart() {
        super.onStart()

        if (deepLinkConsumer == null) {
            deepLinkConsumer = object : DeepLinkConsumer {
                override fun consumeDeepLink(uri: Uri): Boolean {
                    val expected = Config.requireRedirectUrl()
                    if (!uri.scheme.equals(expected, true)) return false

                    controller.handleDeepLink(uri)
                    return true
                }
            }
        }

        DeepLinkRouter.register(deepLinkConsumer!!)
    }

    override fun onStop() {
        deepLinkConsumer?.let { DeepLinkRouter.unregister(it) }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.destroy()
        deepLinkConsumer = null
    }
}