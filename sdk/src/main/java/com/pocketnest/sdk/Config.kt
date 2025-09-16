package com.pocketnest.sdk

object Config {
    private var url: String? = null
    private var redirectUrl: String? = null

    fun init(url: String, redirectUrl: String) {
        this.url = url
        this.redirectUrl = redirectUrl
    }

    internal fun requireUrl(): String =
        url ?: throw IllegalStateException("config not initialized with url")

    internal fun requireRedirectUrl(): String =
        redirectUrl ?: throw IllegalStateException("config not initialized with redirectUrl")
}
