// org/pocketnest/sdk/internal/DeepLinkRouter.kt
package org.pocketnest.sdk.internal

import android.net.Uri
import java.lang.ref.WeakReference

internal interface DeepLinkConsumer {
    fun consumeDeepLink(uri: Uri): Boolean
}

internal object DeepLinkRouter {

    private var consumerRef = WeakReference<DeepLinkConsumer>(null)
    private var pendingUri: Uri? = null

    fun register(consumer: DeepLinkConsumer) {
        consumerRef = WeakReference(consumer)

        // Try to deliver any pending deep link
        pendingUri?.let { uri ->
            if (consumer.consumeDeepLink(uri)) {
                pendingUri = null
            }
            // IMPORTANT: if consumeDeepLink() returns false,
            // we KEEP pendingUri so it can be retried later
        }
    }

    fun unregister(consumer: DeepLinkConsumer) {
        consumerRef.get()?.let {
            if (it === consumer) {
                consumerRef = WeakReference(null)
            }
        }
    }

    /**
     * Route a deep link.
     * - If consumer is ready and consumes it → done
     * - Otherwise store as pending (last one wins)
     */
    fun route(uri: Uri) {
        val consumer = consumerRef.get()
        if (consumer != null && consumer.consumeDeepLink(uri)) {
            pendingUri = null
        } else {
            pendingUri = uri
        }
    }

    /**
     * Backward compatibility for your existing calls
     */
    fun deliver(uri: Uri): Boolean {
        val consumer = consumerRef.get()
        return if (consumer != null && consumer.consumeDeepLink(uri)) {
            pendingUri = null
            true
        } else {
            pendingUri = uri
            false
        }
    }
}