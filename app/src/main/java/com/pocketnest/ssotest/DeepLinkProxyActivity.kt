// DeepLinkProxyActivity.kt
package com.pocketnest.ssotest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class DeepLinkProxyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forward the deep link to the existing WebViewActivity instance
        val forward = Intent(this, WebViewActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = intent?.data
            // Route to existing activity instance if present
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(forward)
        finish() // no UI
    }
}