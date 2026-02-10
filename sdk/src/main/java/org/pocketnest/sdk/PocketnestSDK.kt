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
     * @param redirectUri   Optional This is only required if you want to use your own unique URL scheme and should match those you set in manifestPlaceholders, otherwise it will be automatically set to default value
     * @param onSuccess     Callback invoked when the WebView flow is presented
     * @param onExit        Callback invoked when the WebView flow is closed or dismissed.
     */
    @JvmStatic
    fun webView(
        activity: Activity,
        url: String,
        accessToken:String?  = null,
        redirectUri:String?  = null,
        onSuccess: (() -> Unit)?  = null,
        onExit: (() -> Unit)?  = null
    ) {
        onSuccessCb = onSuccess
        onExitCb = onExit

        Config.init(url, accessToken, redirectUri);
        Config.launchMode = Config.LaunchMode.ACTIVITY
        activity.startActivity(Intent(activity, WebViewActivity::class.java))
    }


    /**
     * Embeddable Fragment for single-activity apps
     *
     * @param url           The URL to load in the WebView.
     * @param accessToken   Optional access token to automatically authenticate the session.
     * @param redirectUri   Optional This is only required if you want to use your own unique URL scheme and should match those you set in manifestPlaceholders, otherwise it will be automatically set to default value
     * @param onSuccess     Callback invoked when the WebView flow is presented
     * @param onExit        Callback invoked when the WebView flow is closed or dismissed.
     */
    @Suppress("unused")
    @JvmStatic
    fun newWebViewFragment(
        url: String,
        accessToken: String?  = null,
        redirectUri: String?  = null,
        onSuccess: (() -> Unit)? = null,
        onExit:    (() -> Unit)? = null
    ): Fragment {
        onSuccessCb = onSuccess
        onExitCb = onExit
        Config.init(url, accessToken, redirectUri)
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