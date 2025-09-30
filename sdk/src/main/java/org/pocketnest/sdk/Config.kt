package org.pocketnest.sdk

object Config {
    enum class LaunchMode { ACTIVITY, FRAGMENT, AUTO }

    @Volatile internal var launchMode: LaunchMode = LaunchMode.AUTO

    private var url: String? = null
    private var redirectUrl: String? = null
    private var accessToken: String? = null

    fun init(url: String, redirectUrl: String?, accessToken:String?) {
        this.url = url
        this.redirectUrl = redirectUrl
        this.accessToken = accessToken
    }

    internal fun requireUrl(): String =
        url ?: throw IllegalStateException("config not initialized with url")

    internal fun requireRedirectUrl(): String? =
        redirectUrl

    internal fun requireAccessToken(): String? =
        accessToken
}
