package org.pocketnest.sdk

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Pocketnest SDK entry point for launching the hosted WebView flow.
 *
 * Provides a static interface (`@JvmStatic`) so it can be easily called
 * from both Kotlin and Java client apps.
 */
object PocketnestSDK {

    // --- Internal state ---

    /**
     * Callback to be invoked when the WebView flow is successfully presented
     * or completed (depending on integration).
     */
    private var onSuccessCb: (() -> Unit)? = null


    /**
     * Callback to be invoked when the WebView flow is closed by the user
     * (e.g. back press or exit button).
     */
    private var onExitCb: (() -> Unit)? = null

    /**
     * Launches the Pocketnest WebView activity.
     *
     * @param activity      The parent [Activity] used to start the flow.
     * @param url           The URL to load in the WebView.
     * @param accessToken   Optional access token to automatically authenticate the session.
     * @param onSuccess     Callback invoked when the WebView flow is presented
     * @param onExit        Callback invoked when the WebView flow is closed or dismissed.
     */
    @JvmStatic
    fun webView(
        activity: Activity,
        url: String,
        accessToken:String?,
        onSuccess: (() -> Unit)?,
        onExit: (() -> Unit)?
    ) {
        onSuccessCb = onSuccess
        onExitCb = onExit

        Config.init(url, accessToken);
        Config.launchMode = Config.LaunchMode.ACTIVITY
        activity.startActivity(Intent(activity, WebViewActivity::class.java))
    }


    /**
     * Embeddable Fragment for single-activity apps
     *
     * @param url           The URL to load in the WebView.
     * @param accessToken   Optional access token to automatically authenticate the session.
     * @param onSuccess     Callback invoked when the WebView flow is presented
     * @param onExit        Callback invoked when the WebView flow is closed or dismissed.
     */
    @Suppress("unused")
    @JvmStatic
    fun newWebViewFragment(
        url: String,
        accessToken: String?,
        onSuccess: (() -> Unit)? = null,
        onExit:    (() -> Unit)? = null
    ): Fragment {
        onSuccessCb = onSuccess
        onExitCb = onExit
        Config.init(url, accessToken)
        Config.launchMode = Config.LaunchMode.FRAGMENT
        return PocketnestWebViewFragment.newInstance()
    }

    internal fun notifyPresented() {
        onSuccessCb?.invoke()
    }

    internal fun notifyClosed() {
        onExitCb?.invoke()
        clear()
    }

    private fun clear() {
        onSuccessCb = null
        onExitCb = null
    }
}