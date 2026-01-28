// org/pocketnest/sdk/DeepLinkProxyActivity.kt
package org.pocketnest.sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.pocketnest.sdk.internal.DeepLinkRouter

class DeepLinkProxyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handle(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handle(intent)
    }

    private fun handle(intent: Intent?) {
        val uri = intent?.data
        val expected = Config.requireRedirectUrl()

        if (uri == null || !uri.scheme.equals(expected, true)) {
            finishNoAnim(); return
        }

        // Always route first (queue or deliver)
        DeepLinkRouter.route(uri)

        // Intermediate return from your webpage:
        // pocketnestredirecturi://return
        val isIntermediateReturn = uri.host.equals("return", true)
        if (isIntermediateReturn) {
            finishNoAnim()
            return
        }

        // FINAL redirect: auto-close Custom Tab by foregrounding the right UI owner
        when (Config.launchMode) {
            Config.LaunchMode.ACTIVITY -> bringWebViewActivityToFront(uri)
            Config.LaunchMode.FRAGMENT,
            Config.LaunchMode.AUTO -> bringHostAppToFront()
        }

        finishNoAnim()
    }

    private fun bringWebViewActivityToFront(uri: android.net.Uri) {
        try {
            startActivity(
                Intent(this, WebViewActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = uri
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
            )
        } catch (_: Exception) {}
    }

    private fun bringHostAppToFront() {
        try {
            val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return
            launch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(launch)
        } catch (_: Exception) {}
    }

    private fun finishNoAnim() {
        finish()
        overridePendingTransition(0, 0)
    }
}