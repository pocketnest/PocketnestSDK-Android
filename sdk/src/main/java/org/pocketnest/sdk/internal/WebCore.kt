// file: org/pocketnest/sdk/internal/WebCore.kt
package org.pocketnest.sdk.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.browser.customtabs.CustomTabsIntent
import org.json.JSONObject
import androidx.core.net.toUri

internal object WebCore {

    internal fun buildUrl(startUrl: String, redirectUri: String?, accessToken: String?): String {
        var u = startUrl
        if (!redirectUri.isNullOrEmpty()) {
            u += if (u.contains("?")) "&redirect_uri=$redirectUri" else "?redirect_uri=$redirectUri"
        }
        if (!accessToken.isNullOrEmpty()) {
            u += if (u.contains("?")) "&token=$accessToken" else "?token=$accessToken"
        }
        return u
    }

    internal fun isExternal(target: Uri, base: String): Boolean {
        val baseHost = base.toUri().host
        val targetHost = target.host
        return baseHost != null && targetHost != null &&
               !baseHost.equals(targetHost, ignoreCase = true)
    }

    internal val BRIDGE_JS = """
        (function(){
          window.HostBridge = window.HostBridge || {};
          window.HostBridge.onHostedLinkComplete = function (payload) {
            try {
              var data = (typeof payload === 'string') ? JSON.parse(payload) : payload;
              window.dispatchEvent(new CustomEvent('hosted-link-complete', { detail: data }));
            } catch (e) { console.error('HostBridge payload parse error', e); }
          };
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
}

/** JS interface for window.native.postMessage(...) */
class NativeBridge(private val onMessage: (String) -> Unit) {
    @android.webkit.JavascriptInterface
    fun postMessage(json: String) = onMessage(json)
}