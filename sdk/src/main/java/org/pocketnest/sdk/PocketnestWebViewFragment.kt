// file: org/pocketnest/sdk/PocketnestWebViewFragment.kt
package org.pocketnest.sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import org.pocketnest.sdk.internal.PocketnestWebController
import org.pocketnest.sdk.internal.DeepLinkConsumer
import org.pocketnest.sdk.internal.DeepLinkRouter

class PocketnestWebViewFragment : Fragment(), DeepLinkConsumer {

    companion object {
        fun newInstance(): PocketnestWebViewFragment = PocketnestWebViewFragment()
    }

    private lateinit var controller: PocketnestWebController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = PocketnestWebController(
            context = requireContext(),
            startUrl = Config.requireUrl(),
            redirectUri = Config.requireRedirectUrl(), // scheme only
            accessToken = Config.requireAccessToken(),
            onPresented = { PocketnestSDK.notifyPresented() },
            onClosed   = { PocketnestSDK.notifyClosed() }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return controller.createWebView(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        DeepLinkRouter.register(this)
        android.util.Log.d("PN-Fragment", "registered")
    }

    override fun onDestroyView() {
        android.util.Log.d("PN-Fragment", "unregister + destroy")
        DeepLinkRouter.unregister(this)
        controller.destroy()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.onSaveInstanceState(outState)
    }

    /** Optional entry if host forwards deep links directly */
    fun handleDeepLinkFromHost(uri: Uri) {
        controller.handleDeepLink(uri)
    }

    override fun consumeDeepLink(uri: Uri): Boolean {
        val expectedScheme = Config.requireRedirectUrl()?.trim()
        android.util.Log.d("PN-Fragment", "consumeDeepLink uri=$uri")
        if (expectedScheme.isNullOrEmpty() || !expectedScheme.equals(uri.scheme, true)) return false
        controller.handleDeepLink(uri)
        return true
    }
}