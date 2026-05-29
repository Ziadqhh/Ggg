package com.example.data.model

import com.example.data.local.SongEntity

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val imageUrl: String,
    val platform: String, // YouTube, Spotify, SoundCloud, Apple Music
    val streamUrl: String,
    val genre: String,
    val isDownloaded: Boolean = false,
    val downloadPath: String? = null,
    val downloadQuality: String? = null,
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): SongEntity {
        return SongEntity(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            imageUrl = imageUrl,
            platform = platform,
            streamUrl = streamUrl,
            genre = genre,
            isDownloaded = isDownloaded,
            downloadPath = downloadPath,
            downloadQuality = downloadQuality,
            isFavorite = isFavorite,
            addedAt = addedAt
        )
    }
}
