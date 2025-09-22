package org.pocketnest.sdk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

class DeepLinkProxyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = intent?.data
        val redirectUrl = Config.requireRedirectUrl()

        if (uri != null && uri.scheme == redirectUrl) {
            val forward = Intent(this, WebViewActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = uri
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(forward)
        }

        finish()
    }
}
