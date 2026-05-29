package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Song
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiMusicClient {
    private const val TAG = "GeminiMusicClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // High fidelity fallback list if Gemini key is missing or calls fail.
    private val fallbackDatabase = listOf(
        Song(
            id = "fb_1",
            title = "Acoustic Sunset Mood",
            artist = "Nora Vance",
            album = "Golden Horizon",
            duration = "5:02",
            imageUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=400",
            platform = "Spotify",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            genre = "Acoustic",
            downloadPath = null,
            downloadQuality = null,
            isDownloaded = false,
            isFavorite = false
        ),
        Song(
            id = "fb_2",
            title = "Lofi Study Session",
            artist = "Chilli Beats",
            album = "Midnight Coffee",
            duration = "6:12",
            imageUrl = "https://images.unsplash.com/photo-1487180142328-054b783fc471?w=400",
            platform = "YouTube",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            genre = "Lofi",
            downloadPath = null,
            downloadQuality = null,
            isDownloaded = false,
            isFavorite = false
        ),
        Song(
            id = "fb_3",
            title = "Synthesized Wave Rider",
            artist = "Neon Dreamer",
            album = "Retroverse",
            duration = "7:05",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400",
            platform = "SoundCloud",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
            genre = "Electronic",
            downloadPath = null,
            downloadQuality = null,
            isDownloaded = false,
            isFavorite = false
        ),
        Song(
            id = "fb_4",
            title = "Cinematic Ambient Forest",
            artist = "Solaris Group",
            album = "Gaia",
            duration = "5:41",
            imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400",
            platform = "Apple Music",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            genre = "Cinematic",
            downloadPath = null,
            downloadQuality = null,
            isDownloaded = false,
            isFavorite = false
        ),
        Song(
            id = "fb_5",
            title = "Smooth Jazz Sax Solo",
            artist = "Marcus K.",
            album = "Blue Note Session",
            duration = "5:18",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400",
            platform = "Spotify",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            genre = "Jazz",
            downloadPath = null,
            downloadQuality = null,
            isDownloaded = false,
            isFavorite = false
        ),
        Song(
            id = "fb_6",
            title = "Rock Anthem",
            artist = "The Distortionists",
            album = "Voltage",
            duration = "5:02",
            imageUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400",
            platform = "YouTube",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            genre = "Rock",
            downloadPath = null,
            downloadQuality = null,
            isDownloaded = false,
            isFavorite = false
        )
    )

    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext fallbackDatabase

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is empty or placeholder! Running fallback query matching...")
            return@withContext performLocalFallbackSearch(query)
        }

        val prompt = """
            You are Maestro Music Expert Bot. The user is searching for songs matching raw query keyword: "$query".
            Generate a JSON array named "songs" containing exactly 6 music tracks that fit or represent this search term. They can match the title, artist, or style/feeling (either in English or Arabic).
            You MUST integrate and simulate high-fidelity results originating from a range of platforms: YouTube, Spotify, SoundCloud, Apple Music.
            Each song in the array must strictly have these fields:
            - id: a unique persistent identifier (like a random string e.g. "gm_12345")
            - title: song title
            - artist: artist name
            - album: album name or "Single"
            - duration: duration like "5:12" or "6:24" (make it close to the streamable file sizes, between 4 to 8 minutes)
            - imageUrl: an Unsplash URL for high quality music cover matching the genre. Use only stable images like:
              - https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400 (Concert lights / Pop / Rock)
              - https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400 (DJ mixer / Electronic / Club)
              - https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=400 (Electric Guitar / Acoustic)
              - https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400 (Microphone / Vocals)
              - https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=400 (Stage performance / Rock)
              - https://images.unsplash.com/photo-1487180142328-054b783fc471?w=400 (Vintage player / Lofi / Indie)
              - https://images.unsplash.com/photo-1506157786151-b8491531f063?w=400 (Psychedelic colors / Hip-hop)
              - https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400 (Art abstract / Classical)
            - platform: value must be one of: "Spotify", "YouTube", "SoundCloud", "Apple Music"
            - streamUrl: Choose ONLY one of the working mp3 audio streams below, matching the closest music vibe:
              1. Lofi Vibe: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
              2. Pop / Electronic: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
              3. Guitar / Acoustic: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
              4. Rock / High-Energy: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
              5. Calm Chill / Cinematic: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
              6. Smooth Synth: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
              7. Jazz Lounge: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
            - genre: one of: "Pop", "Lofi", "Acoustic", "Rock", "Electronic", "Jazz", "Cinematic", "Hip-Hop"
            
            Format response as a raw JSON object containing the "songs" array only. Do not wrap response in markdown blocks (like ```json), write raw JSON string. Let search terms feel extremely realistic, professional, and matching user's query perfectly.
        """.trimIndent()

        try {
            val systemInstruction = "You are a precise, JSON-only returning server that parses music search inputs and outputs detailed, responsive results for an Android player client."
            
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Search api call failed: code=${response.code}, message=${response.message}")
                return@withContext performLocalFallbackSearch(query)
            }

            val responseBody = response.body?.string() ?: return@withContext performLocalFallbackSearch(query)
            Log.d(TAG, "API Raw response: $responseBody")

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")

            val songsJson = JSONObject(text).getJSONArray("songs")
            val resultSongs = mutableListOf<Song>()
            for (i in 0 until songsJson.length()) {
                val item = songsJson.getJSONObject(i)
                resultSongs.add(
                    Song(
                        id = item.optString("id", "g_${System.currentTimeMillis()}_$i"),
                        title = item.optString("title", "Unknown Track"),
                        artist = item.optString("artist", "Unknown Artist"),
                        album = item.optString("album", "Single"),
                        duration = item.optString("duration", "4:30"),
                        imageUrl = item.optString("imageUrl", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400"),
                        platform = item.optString("platform", "Spotify"),
                        streamUrl = item.optString("streamUrl", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
                        genre = item.optString("genre", "Pop"),
                        isDownloaded = false,
                        downloadPath = null,
                        downloadQuality = null,
                        isFavorite = false
                    )
                )
            }
            return@withContext resultSongs
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Gemini API, falling back safely", e)
            return@withContext performLocalFallbackSearch(query)
        }
    }

    private fun performLocalFallbackSearch(query: String): List<Song> {
        val lowercaseQuery = query.lowercase().trim()
        val results = fallbackDatabase.filter {
            it.title.lowercase().contains(lowercaseQuery) ||
            it.artist.lowercase().contains(lowercaseQuery) ||
            it.genre.lowercase().contains(lowercaseQuery) ||
            query.contains(it.genre) ||
            (lowercaseQuery.contains("حزن") && it.genre == "Cinematic") ||
            (lowercaseQuery.contains("هدوء") && (it.genre == "Lofi" || it.genre == "Acoustic")) ||
            (lowercaseQuery.contains("رياض") || lowercaseQuery.contains("حماس") && it.genre == "Rock") ||
            (lowercaseQuery.contains("طرب") || lowercaseQuery.contains("مزاج") && it.genre == "Jazz")
        }
        return results.ifEmpty {
            // Generate some clever matched tracks on the fly to avoid returning empty
            listOf(
                Song(
                    id = "dyn_1",
                    title = "Matching Horizon : '$query'",
                    artist = "Vibe Architect",
                    album = "Custom Search Vibe",
                    duration = "5:02",
                    imageUrl = "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=400",
                    platform = "YouTube",
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    genre = "Pop",
                    downloadPath = null,
                    downloadQuality = null,
                    isDownloaded = false,
                    isFavorite = false
                ),
                Song(
                    id = "dyn_2",
                    title = "Deep Resonance Studio",
                    artist = "Sonic Scout",
                    album = "Sound Wave Scout",
                    duration = "6:12",
                    imageUrl = "https://images.unsplash.com/photo-1487180142328-054b783fc471?w=400",
                    platform = "Spotify",
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    genre = "Lofi",
                    downloadPath = null,
                    downloadQuality = null,
                    isDownloaded = false,
                    isFavorite = false
                )
            )
        }
    }
}
