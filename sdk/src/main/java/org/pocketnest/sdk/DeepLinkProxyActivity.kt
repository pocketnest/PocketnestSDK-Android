// org/pocketnest/sdk/DeepLinkProxyActivity.kt
package org.pocketnest.sdk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import org.pocketnest.sdk.internal.DeepLinkRouter

// DeepLinkProxyActivity.kt
class DeepLinkProxyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        val expected = Config.requireRedirectUrl()
        val mode = Config.launchMode
        if (!uri?.scheme.equals(expected, true)) {
            finish(); overridePendingTransition(0,0); return
        }

        when (mode) {
            Config.LaunchMode.FRAGMENT -> {
                // Deliver to fragment (or queue)
                var delivered = org.pocketnest.sdk.internal.DeepLinkRouter.deliver(uri!!)
                // Bring the app’s launcher activity to front to close CCT
                packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
                    launch.addFlags(
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                    startActivity(launch)
                }
                if (!delivered) {
                    window.decorView.postDelayed({
                        org.pocketnest.sdk.internal.DeepLinkRouter.deliver(uri)
                        finish(); overridePendingTransition(0,0)
                    }, 200)
                } else {
                    finish(); overridePendingTransition(0,0)
                }
            }

            Config.LaunchMode.ACTIVITY -> {
                // Direct the deep link to the existing WebViewActivity
                startActivity(
                    Intent(this, org.pocketnest.sdk.WebViewActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = uri
                        addFlags(
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        )
                    }
                )
                finish(); overridePendingTransition(0,0)
            }

            Config.LaunchMode.AUTO -> {
                // Prefer fragment, fallback to activity after short delay
                var delivered = org.pocketnest.sdk.internal.DeepLinkRouter.deliver(uri!!)
                if (delivered) {
                    packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
                        launch.addFlags(
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                        startActivity(launch)
                    }
                    finish(); overridePendingTransition(0,0)
                } else {
                    window.decorView.postDelayed({
                        if (!org.pocketnest.sdk.internal.DeepLinkRouter.deliver(uri)) {
                            startActivity(
                                Intent(this, org.pocketnest.sdk.WebViewActivity::class.java).apply {
                                    action = Intent.ACTION_VIEW
                                    data = uri
                                    addFlags(
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    )
                                }
                            )
                        }
                        finish(); overridePendingTransition(0,0)
                    }, 200)
                }
            }
        }
    }
}