package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val imageUrl: String,
    val platform: String,
    val streamUrl: String,
    val genre: String,
    val isDownloaded: Boolean,
    val downloadPath: String?,
    val downloadQuality: String?,
    val isFavorite: Boolean,
    val addedAt: Long
) {
    fun toDomain(): Song {
        return Song(
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
