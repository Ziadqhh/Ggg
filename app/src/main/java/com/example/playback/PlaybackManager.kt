package com.example.playback

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.data.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlaybackManager {
    private const val TAG = "PlaybackManager"
    private var mediaPlayer: MediaPlayer? = null
    
    // Core states
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f) // 0.0 to 1.0f
    val playbackProgress = _playbackProgress.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0)
    val durationMs = _durationMs.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError = _playbackError.asStateFlow()

    // Playlist queue
    private var currentQueue = listOf<Song>()
    private var currentQueueIndex = -1

    private var progressJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        setupMediaPlayer()
    }

    private fun setupMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _durationMs.value = mp.duration
                    _playbackError.value = null
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackProgress.value = 1.0f
                    stopProgressTracker()
                    playNext()
                }
                setOnErrorListener { _, what, extra ->
                    val errorMsg = "Media player error: what=$what, extra=$extra"
                    Log.e(TAG, errorMsg)
                    _playbackError.value = "Failed to stream audio file. Check your internet connection."
                    _isPlaying.value = false
                    stopProgressTracker()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create MediaPlayer", e)
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        if (queue.isNotEmpty()) {
            currentQueue = queue
            currentQueueIndex = queue.indexOfFirst { it.id == song.id }
        } else if (!currentQueue.any { it.id == song.id }) {
            currentQueue = listOf(song)
            currentQueueIndex = 0
        } else {
            currentQueueIndex = currentQueue.indexOfFirst { it.id == song.id }
        }

        _playbackError.value = null
        stopProgressTracker()

        try {
            _currentSong.value = song
            _isPlaying.value = false
            _playbackProgress.value = 0f
            _currentPositionMs.value = 0

            mediaPlayer?.reset()
            // Set streaming URL or cache URL
            val sourceUrl = song.downloadPath ?: song.streamUrl
            mediaPlayer?.setDataSource(sourceUrl)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio playback", e)
            _playbackError.value = "Could not initialize stream: ${e.localizedMessage}"
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        val current = _currentSong.value ?: return

        if (mp.isPlaying) {
            mp.pause()
            _isPlaying.value = false
            stopProgressTracker()
        } else {
            // Check if error is present, if so, replay
            if (_playbackError.value != null) {
                playSong(current)
            } else {
                mp.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        }
    }

    fun seekTo(progress: Float) {
        val mp = mediaPlayer ?: return
        val duration = _durationMs.value
        if (duration > 0) {
            val positionMs = (progress * duration).toInt()
            mp.seekTo(positionMs)
            _currentPositionMs.value = positionMs
            _playbackProgress.value = progress
        }
    }

    fun skipForward() {
        val mp = mediaPlayer ?: return
        val newPos = (mp.currentPosition + 10000).coerceAtMost(mp.duration)
        mp.seekTo(newPos)
        _currentPositionMs.value = newPos
        _playbackProgress.value = newPos.toFloat() / mp.duration.toFloat()
    }

    fun skipBackward() {
        val mp = mediaPlayer ?: return
        val newPos = (mp.currentPosition - 10000).coerceAtLeast(0)
        mp.seekTo(newPos)
        _currentPositionMs.value = newPos
        _playbackProgress.value = newPos.toFloat() / mp.duration.toFloat()
    }

    fun playNext() {
        if (currentQueue.isEmpty() || currentQueueIndex == -1) return
        val nextIndex = (currentQueueIndex + 1) % currentQueue.size
        playSong(currentQueue[nextIndex], currentQueue)
    }

    fun playPrevious() {
        if (currentQueue.isEmpty() || currentQueueIndex == -1) return
        var prevIndex = currentQueueIndex - 1
        if (prevIndex < 0) prevIndex = currentQueue.size - 1
        playSong(currentQueue[prevIndex], currentQueue)
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                delay(250)
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val pos = mp.currentPosition
                        val duration = mp.duration
                        _currentPositionMs.value = pos
                        if (duration > 0) {
                            _playbackProgress.value = pos.toFloat() / duration.toFloat()
                        }
                    }
                }
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun cleanUp() {
        stopProgressTracker()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
