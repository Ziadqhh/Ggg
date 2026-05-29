package com.spotify.android.appremote.api

class ConnectionParams private constructor(
    val clientId: String,
    val redirectUri: String,
    val showAuthView: Boolean
) {
    class Builder(private val clientId: String) {
        private var redirectUri: String = ""
        private var showAuthView: Boolean = false

        fun setRedirectUri(uri: String): Builder {
            this.redirectUri = uri
            return this
        }

        fun showAuthView(show: Boolean): Builder {
            this.showAuthView = show
            return this
        }

        fun build(): ConnectionParams {
            return ConnectionParams(clientId, redirectUri, showAuthView)
        }
    }
}
