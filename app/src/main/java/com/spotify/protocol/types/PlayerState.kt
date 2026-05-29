package com.spotify.protocol.types

data class PlayerState(
    val track: Track,
    val isPaused: Boolean = false,
    val playbackPosition: Long = 0L
)
