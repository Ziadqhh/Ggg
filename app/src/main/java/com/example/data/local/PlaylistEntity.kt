package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val isSmart: Boolean = false,
    val smartType: String? = null // "FAVORITES", "DOWNLOADS", "YT_ONLY", "GENRE"
)
