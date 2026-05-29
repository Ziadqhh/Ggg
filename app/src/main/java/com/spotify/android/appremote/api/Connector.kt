package com.spotify.android.appremote.api

interface Connector {
    interface ConnectionListener {
        fun onConnected(appRemote: SpotifyAppRemote)
        fun onFailure(throwable: Throwable)
    }
}
