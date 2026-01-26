package org.pocketnest.sdk

object Config {
    enum class LaunchMode { ACTIVITY, FRAGMENT, AUTO }

    @Volatile internal var launchMode: LaunchMode = LaunchMode.AUTO

    private var url: String? = null
    private var redirectUrl: String = "pocketnestredirecturi" // Static value
    private var accessToken: String? = null

    fun init(url: String, accessToken:String?) {
        this.url = url
        this.accessToken = accessToken
    }

    internal fun requireUrl(): String =
        url ?: throw IllegalStateException("config not initialized with url")

    internal fun requireRedirectUrl(): String? =
        redirectUrl

    internal fun requireAccessToken(): String? =
        accessToken
}
