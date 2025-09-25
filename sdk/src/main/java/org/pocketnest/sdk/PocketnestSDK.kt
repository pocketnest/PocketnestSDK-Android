package org.pocketnest.sdk

import android.app.Activity
import android.content.Intent

object PocketnestSDK {

    private var onSuccessCb: ((Map<String, Any?>) -> Unit)? = null
    private var onExitCb: (() -> Unit)? = null

    @JvmStatic
    fun webView(
        activity: Activity,
        url: String,
        redirectUri: String,
        onSuccess: (Map<String, Any?>) -> Unit,
        onExit: () -> Unit
    ) {
        onSuccessCb = onSuccess
        onExitCb = onExit

        // Initialize the SDK configuration with the provided URL and redirect URI.
        Config.init(url, redirectUri)

        activity.startActivity(
            Intent(activity, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.EXTRA_URL, url)
                putExtra(WebViewActivity.EXTRA_REDIRECT_SCHEME, redirectUri)
            }
        )
    }

    internal fun notifySuccess(data: Map<String, Any?>) {
        onSuccessCb?.invoke(data)
        clear()
    }

    internal fun notifyExit() {
        onExitCb?.invoke()
        clear()
    }

    private fun clear() {
        onSuccessCb = null
        onExitCb = null
    }
}