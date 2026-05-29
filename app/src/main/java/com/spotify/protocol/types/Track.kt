package com.spotify.protocol.types

data class Track(
    val name: String,
    val artist: Artist,
    val uri: String = ""
)
