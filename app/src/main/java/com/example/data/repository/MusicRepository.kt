package com.example.data.repository

import com.example.data.api.GeminiMusicClient
import com.example.data.local.MusicDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongCrossRef
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(private val musicDao: MusicDao) {

    // Fetch all metadata cached or downloaded locally
    val allSongs: Flow<List<Song>> = musicDao.getAllSongs().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoriteSongs: Flow<List<Song>> = musicDao.getFavoriteSongs().map { entities ->
        entities.map { it.toDomain() }
    }

    val downloadedSongs: Flow<List<Song>> = musicDao.getDownloadedSongs().map { entities ->
        entities.map { it.toDomain() }
    }

    val allPlaylists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()

    fun getSongsForPlaylist(playlistId: Int): Flow<List<Song>> {
        return musicDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun searchSongs(query: String): List<Song> {
        // Query Gemini API
        val remoteSongs = GeminiMusicClient.searchSongs(query)
        
        // Match with local state (e.g. if a song has already been downloaded or favorited locally)
        return remoteSongs.map { remoteSong ->
            val localEntity = musicDao.getSongByIdDirect(remoteSong.id)
            if (localEntity != null) {
                localEntity.toDomain()
            } else {
                remoteSong
            }
        }
    }

    suspend fun saveSong(song: Song) {
        musicDao.insertSong(song.toEntity())
    }

    suspend fun toggleFavorite(song: Song) {
        val updatedSong = song.copy(isFavorite = !song.isFavorite)
        musicDao.insertSong(updatedSong.toEntity())
    }

    suspend fun setDownloaded(song: Song, quality: String, downloadPath: String) {
        val updatedSong = song.copy(
            isDownloaded = true,
            downloadQuality = quality,
            downloadPath = downloadPath
        )
        musicDao.insertSong(updatedSong.toEntity())
        
        // If it's downloaded, check if we should associate it with the smart "Downloads" playlist if any
    }

    suspend fun createPlaylist(name: String, description: String, isSmart: Boolean = false, smartType: String? = null): Long {
        return musicDao.insertPlaylist(PlaylistEntity(name = name, description = description, isSmart = isSmart, smartType = smartType))
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        musicDao.clearPlaylistSongs(playlist.id)
        musicDao.deletePlaylist(playlist)
    }

    suspend fun addSongToPlaylist(song: Song, playlistId: Int) {
        // First ensure safety by saving the song metadata in the db
        musicDao.insertSong(song.toEntity())
        // Join them
        musicDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, song.id))
    }

    suspend fun removeSongFromPlaylist(songId: String, playlistId: Int) {
        musicDao.deletePlaylistSongCrossRef(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun seedInitialSmartPlaylists() {
        // Seed default playlists if they do not exist
        // This coordinates smart filters requested by the user
        // We will do this in the ViewModel setup
    }
}
