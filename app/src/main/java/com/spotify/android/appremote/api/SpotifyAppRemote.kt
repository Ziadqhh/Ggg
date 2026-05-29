package com.spotify.android.appremote.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

class SpotifyAppRemote {
    val playerApi: PlayerApi = PlayerApi()

    companion object {
        private const val TAG = "SpotifyAppRemote"
        private var activeRemote: SpotifyAppRemote? = null

        // Callback hooks for the app to display live indicators
        var onPlayUriListener: ((String) -> Unit)? = null
        var onConnectionStateListener: ((Boolean) -> Unit)? = null

        fun connect(
            context: Context,
            connectionParams: ConnectionParams,
            listener: Connector.ConnectionListener
        ) {
            Log.d(TAG, "Connecting to Spotify App Remote with clientID: ${connectionParams.clientId}")

            // Simulating connecting asynchronously to match real SDK behavior
            Handler(Looper.getMainLooper()).postDelayed({
                if (connectionParams.clientId.isEmpty() || connectionParams.clientId == "your_client_id") {
                    listener.onFailure(Exception("Invalid or empty Client ID! Setup your Spotify credentials inside MainActivity first."))
                    onConnectionStateListener?.invoke(false)
                } else {
                    val remote = SpotifyAppRemote()
                    activeRemote = remote
                    listener.onConnected(remote)
                    onConnectionStateListener?.invoke(true)

                    // Trigger initial player state event callback to simulate a successful subscription response
                    Handler(Looper.getMainLooper()).postDelayed({
                        val initialTrack = Track(
                            name = "Blinding Lights",
                            artist = Artist("The Weeknd"),
                            uri = "spotify:track:4H7O96l7Z1KqX8Xqf2R7mP"
                        )
                        remote.playerApi.triggerPlayerStateChange(PlayerState(initialTrack))
                    }, 500)
                }
            }, 1000)
        }

        fun disconnect(appRemote: SpotifyAppRemote) {
            Log.d(TAG, "Disconnecting from Spotify App Remote")
            activeRemote = null
            onConnectionStateListener?.invoke(false)
        }

        fun notifyPlayUri(uri: String) {
            onPlayUriListener?.invoke(uri)

            // Generate simulated callback event representing the new song
            activeRemote?.let { remote ->
                val (trackName, artistName) = parseUriMetadata(uri)
                val track = Track(name = trackName, artist = Artist(artistName), uri = uri)
                remote.playerApi.triggerPlayerStateChange(PlayerState(track))
            }
        }

        private fun parseUriMetadata(uri: String): Pair<String, String> {
            return when {
                uri == "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL" -> Pair("Indie Feel Good Mix", "Indie Artists")
                uri == "spotify:playlist:37i9dQZF1DX7K31D69s4M1" -> Pair("Serene Piano Resonance", "Aura Keyboard Ensemble")
                uri.contains("track") -> Pair("Save Your Tears", "The Weeknd")
                else -> Pair("Spotify Premium Playlist Stream", "Spotify Editorial")
            }
        }
    }
}

class PlayerApi {
    private val subscription = Subscription<PlayerState>()
    private var lastPlayedUri: String = ""

    fun play(uri: String) {
        lastPlayedUri = uri
        SpotifyAppRemote.notifyPlayUri(uri)
    }

    fun subscribeToPlayerState(): Subscription<PlayerState> {
        return subscription
    }

    fun triggerPlayerStateChange(state: PlayerState) {
        subscription.triggerEvent(state)
    }
}
