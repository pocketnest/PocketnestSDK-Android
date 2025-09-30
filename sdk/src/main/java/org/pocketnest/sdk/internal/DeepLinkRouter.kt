// org/pocketnest/sdk/internal/DeepLinkRouter.kt
package org.pocketnest.sdk.internal

import android.net.Uri
import java.lang.ref.WeakReference

internal interface DeepLinkConsumer { fun consumeDeepLink(uri: Uri): Boolean }

internal object DeepLinkRouter {
    private var consumerRef = java.lang.ref.WeakReference<DeepLinkConsumer>(null)
    private var pendingUri: android.net.Uri? = null

    fun register(consumer: DeepLinkConsumer) {
        consumerRef = java.lang.ref.WeakReference(consumer)
        pendingUri?.let { if (consumer.consumeDeepLink(it)) pendingUri = null }
    }
    fun unregister(consumer: DeepLinkConsumer) {
        consumerRef.get()?.let { if (it === consumer) consumerRef = java.lang.ref.WeakReference(null) }
    }
    fun deliver(uri: android.net.Uri): Boolean {
        val c = consumerRef.get()
        return if (c != null && c.consumeDeepLink(uri)) true
        else { pendingUri = uri; false }
    }
}