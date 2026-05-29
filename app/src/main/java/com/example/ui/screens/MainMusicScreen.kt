package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import kotlin.random.Random
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.PlaylistEntity
import com.example.data.model.Song
import com.example.ui.MusicViewModel
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import com.spotify.protocol.types.Artist
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// Theme Custom Premium Colors
val DeepObsidian = Color(0xFF0A0A0B) // Sophisticated absolute dark background
val CardSlate = Color(0xFF141416) // Rich premium dark card surface
val NeonCyan = Color(0xFF8B5CF6) // Royal Lavender/Purple premium accent
val NeonCoral = Color(0xFFEC407A) // Soft vibrant rose/pink for favorites/heart cues
val GoldAccent = Color(0xFF34D399) // Clean Emerald green for high quality/smart label accents
val SoftGrey = Color(0xFF94A3B8) // Elegantly muted slate for secondary descriptions and details

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainMusicScreen(viewModel: MusicViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) } // 0: Explore, 1: Playlists, 2: Downloads
    var selectedPlatformFilter by remember { mutableStateOf("All") }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchError by viewModel.searchError.collectAsStateWithLifecycle()

    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val downloadedSongs by viewModel.downloadedSongs.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playlistSongs by viewModel.playlistSongs.collectAsStateWithLifecycle()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    var showPlaylistSongsDialog by remember { mutableStateOf<PlaylistEntity?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showQualitySelectorSheet by remember { mutableStateOf<Song?>(null) }
    var showAddToPlaylistSheet by remember { mutableStateOf<Song?>(null) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    // Seed/filter search results with platform filters
    val filteredSearchResults = remember(searchResults, selectedPlatformFilter) {
        if (selectedPlatformFilter == "All") {
            searchResults
        } else {
            searchResults.filter { it.platform.equals(selectedPlatformFilter, ignoreCase = true) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // Main Screen Scaffold (excluding full player overlay)
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Header Brand
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier
                                    .size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Melody Pro",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD1D1D1)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Maestro",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftGrey
                            )
                        }

                        // Circular User Avatar Box
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1C1C1E))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftGrey
                            )
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Tab Controller
                Column {
                    // Spacer for miniplayer area to avoid overlaps
                    if (currentSong != null) {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                    
                    NavigationBar(
                        containerColor = Color.Black.copy(alpha = 0.85f),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .border(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Icon(imageVector = Icons.Default.Explore, contentDescription = "Explore") },
                            label = { Text("استكشاف", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = SoftGrey,
                                unselectedTextColor = SoftGrey,
                                indicatorColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("tab_explore")
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Icon(imageVector = Icons.Default.LibraryMusic, contentDescription = "Library") },
                            label = { Text("مكتبتي", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = SoftGrey,
                                unselectedTextColor = SoftGrey,
                                indicatorColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("tab_library")
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = { Icon(imageVector = Icons.Default.DownloadForOffline, contentDescription = "Downloads") },
                            label = { Text("العينات", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = SoftGrey,
                                unselectedTextColor = SoftGrey,
                                indicatorColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("tab_downloads")
                        )
                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { currentTab = 3 },
                            icon = { Icon(imageVector = Icons.Default.Cast, contentDescription = "Spotify Remote") },
                            label = { Text("Spotify", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1DB954),
                                selectedTextColor = Color(0xFF1DB954),
                                unselectedIconColor = SoftGrey,
                                unselectedTextColor = SoftGrey,
                                indicatorColor = Color(0xFF1DB954).copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("tab_spotify")
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    0 -> ExploreView(
                        viewModel = viewModel,
                        filteredSearchResults = filteredSearchResults,
                        isSearching = isSearching,
                        searchQuery = searchQuery,
                        selectedPlatformFilter = selectedPlatformFilter,
                        searchError = searchError,
                        onFilterChanged = { selectedPlatformFilter = it },
                        onDownloadClick = { showQualitySelectorSheet = it },
                        onAddToPlaylistClick = { showAddToPlaylistSheet = it }
                    )
                    1 -> PlaylistsView(
                        playlists = playlists,
                        favoriteCount = favoriteSongs.size,
                        downloadsCount = downloadedSongs.size,
                        onPlaylistClick = { playlist ->
                            showPlaylistSongsDialog = playlist
                            viewModel.loadPlaylistSongs(playlist)
                        },
                        onCreatePlaylistClick = { showCreatePlaylistDialog = true }
                    )
                    2 -> DownloadsView(
                        viewModel = viewModel,
                        downloadedSongs = downloadedSongs,
                        onAddToPlaylistClick = { showAddToPlaylistSheet = it }
                    )
                    3 -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        SpotifyDeveloperPanel(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Mini Hovering Player (Sticky at the bottom above back stack)
        if (currentSong != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Height of bottom bar
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                MiniPlayerBar(
                    currentSong = currentSong!!,
                    isPlaying = isPlaying,
                    progress = viewModel.playbackProgress.collectAsStateWithLifecycle().value,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onSkipNext = { viewModel.playNext() },
                    onExpandPlayer = { isPlayerExpanded = true }
                )
            }
        }

        // Expanded Glassmorphic Player Full Screen Deck
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessMedium))
        ) {
            currentSong?.let { activeSong ->
                FullScreenPlayer(
                    song = activeSong,
                    isPlaying = isPlaying,
                    progress = viewModel.playbackProgress.collectAsStateWithLifecycle().value,
                    currentPositionMs = viewModel.currentPositionMs.collectAsStateWithLifecycle().value,
                    durationMs = viewModel.durationMs.collectAsStateWithLifecycle().value,
                    playbackError = viewModel.playbackError.collectAsStateWithLifecycle().value,
                    onMinimize = { isPlayerExpanded = false },
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onPrevious = { viewModel.playPrevious() },
                    onNext = { viewModel.playNext() },
                    onSkipForward = { viewModel.skipForward() },
                    onSkipBackward = { viewModel.skipBackward() },
                    onSeek = { viewModel.seekTo(it) },
                    onAddToPlaylist = { showAddToPlaylistSheet = activeSong }
                )
            }
        }

        // Dialog for viewing playlist contents
        if (showPlaylistSongsDialog != null) {
            val pl = showPlaylistSongsDialog!!
            PlaylistSongsDialog(
                playlist = pl,
                songs = playlistSongs,
                activeSongId = currentSong?.id,
                isPlaying = isPlaying,
                onDismiss = { showPlaylistSongsDialog = null },
                onSongSelect = { select ->
                    viewModel.playSong(select, playlistSongs)
                },
                onDeleteSongFromPlaylist = { targetSong ->
                    viewModel.removeSongFromPlaylist(targetSong.id, pl.id)
                },
                onDeletePlaylist = {
                    viewModel.deletePlaylist(pl)
                    showPlaylistSongsDialog = null
                    Toast.makeText(context, "تم حذف قائمة التشغيل", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Form to create new playlist
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { showCreatePlaylistDialog = false },
                onCreate = { name, desc ->
                    viewModel.addNewPlaylist(name, desc)
                    showCreatePlaylistDialog = false
                    Toast.makeText(context, "تم انشاء قائمة التشغيل الخاصة بك", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Bottom sheets styled using customized compose overlays
        if (showQualitySelectorSheet != null) {
            val song = showQualitySelectorSheet!!
            QualitySelectorDialog(
                song = song,
                onDismiss = { showQualitySelectorSheet = null },
                onChooseQuality = { quality ->
                    viewModel.downloadSong(song, quality)
                    showQualitySelectorSheet = null
                    Toast.makeText(context, "بدء تحميل ${song.title} بجودة $quality", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showAddToPlaylistSheet != null) {
            val song = showAddToPlaylistSheet!!
            AddToPlaylistDialog(
                playlists = playlists.filter { !it.isSmart }, // custom user lists
                onDismiss = { showAddToPlaylistSheet = null },
                onChoosePlaylist = { playlistId ->
                    viewModel.addSongToPlaylist(song, playlistId)
                    showAddToPlaylistSheet = null
                    Toast.makeText(context, "تمت الإضافة لقائمة التشغيل لـ ${song.title}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// ==========================================
// SCREEN SUB-COMPOSABLES (TABS)
// ==========================================

@Composable
fun ExploreView(
    viewModel: MusicViewModel,
    filteredSearchResults: List<Song>,
    isSearching: Boolean,
    searchQuery: String,
    selectedPlatformFilter: String,
    searchError: String?,
    onFilterChanged: (String) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val downloadProgresses by viewModel.downloadProgresses.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Multi-Platform Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("ابحث عن أي أغنية، فنان، أو نصف اسم دافيء...", color = SoftGrey, fontSize = 14.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NeonCyan) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = SoftGrey)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Bot Status",
                        tint = if (isSearching) NeonCoral else NeonCyan,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .scale(if (isSearching) 1.2f else 1.0f)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.performSearch(searchQuery)
                keyboardController?.hide()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = SoftGrey.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = CardSlate,
                unfocusedContainerColor = CardSlate
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("song_search_input")
        )

        // Platform filter tabs
        val platforms = listOf("All", "Spotify", "YouTube", "SoundCloud", "Apple Music")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(platforms) { platform ->
                val selected = selectedPlatformFilter == platform
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (selected) Color.White else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { onFilterChanged(platform) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Display matching icon for aesthetic premium touch
                        Icon(
                            imageVector = when (platform) {
                                "Spotify" -> Icons.Default.Radio
                                "YouTube" -> Icons.Default.PlayCircle
                                "SoundCloud" -> Icons.Default.Cloud
                                "Apple Music" -> Icons.Default.MusicNote
                                else -> Icons.Default.AllInclusive
                            },
                            contentDescription = null,
                            tint = if (selected) Color.Black else SoftGrey,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = platform,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.Black else Color(0xFFD1D1D1)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Results List / States
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan, strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جاري الاتصال بـ الذكاء الاصطناعي وجلب النتائج...",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (searchError != null && filteredSearchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicVideo,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = SoftGrey.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = searchError,
                        color = SoftGrey,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredSearchResults, key = { it.id }) { song ->
                    SongCardItem(
                        song = song,
                        isDownloading = downloadProgresses.containsKey(song.id),
                        downloadProgress = downloadProgresses[song.id] ?: 0f,
                        onPlayClick = { viewModel.playSong(song, filteredSearchResults) },
                        onFavoriteClick = { viewModel.toggleFavorite(song) },
                        onDownloadClick = { onDownloadClick(song) },
                        onAddToPlaylistClick = { onAddToPlaylistClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistsView(
    playlists: List<PlaylistEntity>,
    favoriteCount: Int,
    downloadsCount: Int,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Active Smart Library summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorites Smart Panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(NeonCoral.copy(alpha = 0.15f), CardSlate)))
                    .clickable {
                        val favPl = playlists.firstOrNull { it.smartType == "FAVORITES" }
                        if (favPl != null) onPlaylistClick(favPl)
                    }
                    .padding(16.dp)
            ) {
                Column {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = NeonCoral, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("المفضلة الذكية", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("$favoriteCount ملفات هامة", fontSize = 12.sp, color = SoftGrey)
                }
            }

            // Cached Offline Smart Panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.15f), CardSlate)))
                    .clickable {
                        val dldPl = playlists.firstOrNull { it.smartType == "DOWNLOADS" }
                        // Fall back to showing favorites if not found, or trigger offline items
                    }
                    .padding(16.dp)
            ) {
                Column {
                    Icon(imageVector = Icons.Default.DownloadDone, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("عينات محملة واعدة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("$downloadsCount تسجيلات كاملة", fontSize = 12.sp, color = SoftGrey)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Lists & Creator row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("قوائم التشغيل الذكية و المخصصة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(
                onClick = onCreatePlaylistClick,
                modifier = Modifier
                    .background(CardSlate, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create", tint = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lists array
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(playlists) { playlist ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .background(CardSlate)
                        .clickable { onPlaylistClick(playlist) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (playlist.isSmart) {
                                            Brush.horizontalGradient(listOf(NeonCyan, NeonCoral))
                                        } else {
                                            Brush.horizontalGradient(listOf(SoftGrey.copy(0.3f), SoftGrey))
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (playlist.isSmart) Icons.Default.Bolt else Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(playlist.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    if (playlist.isSmart) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White.copy(0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("SMART", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                        }
                                    }
                                }
                                Text(playlist.description, fontSize = 11.sp, color = SoftGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SoftGrey)
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadsView(
    viewModel: MusicViewModel,
    downloadedSongs: List<Song>,
    onAddToPlaylistClick: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Space details stats
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(CardSlate, CardSlate.copy(alpha = 0.6f))))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الذاكرة الموسيقية غير المتصلة (الذاتية)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("الأغاني المحملة والمسارات المخبأة مسبقاً تعمل بشكل كلي بدون انترنت", fontSize = 12.sp, color = SoftGrey)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = 0.15f,
                    color = NeonCyan,
                    trackColor = Color.White.copy(0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("جميع عيناتي الصوتية المستقرة", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        if (downloadedSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(54.dp), tint = SoftGrey.copy(0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد تحميلات نشطة بعد. ابحث في استكشاف وقم بحفظ العينات!", color = SoftGrey, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(downloadedSongs) { song ->
                    SongCardItem(
                        song = song,
                        isDownloading = false,
                        downloadProgress = 0f,
                        onPlayClick = { viewModel.playSong(song, downloadedSongs) },
                        onFavoriteClick = { viewModel.toggleFavorite(song) },
                        onDownloadClick = {}, // already downloaded
                        onAddToPlaylistClick = { onAddToPlaylistClick(song) }
                    )
                }
            }
        }
    }
}

// ==========================================
// CUSTOM UI CARD & LIST HELPER ITEMS
// ==========================================

@Composable
fun SongCardItem(
    song: Song,
    isDownloading: Boolean,
    downloadProgress: Float,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit
) {
    val progressPercent = (downloadProgress * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSlate)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Title Information Block
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cover Arts
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = song.imageUrl,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Platform tag overlay in miniaturized version
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(CardSlate)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = song.platform.take(2).uppercase(),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = song.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        color = SoftGrey,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldAccent.copy(alpha = 0.1f))
                                .border(1.dp, GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(song.genre, fontSize = 9.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(song.duration, fontSize = 10.sp, color = SoftGrey)
                        
                        if (song.isDownloaded) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.FileDownloadDone, contentDescription = "Downloaded", tint = GoldAccent, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            // Interactive Actions Block
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Favorite love trigger
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) NeonCoral else SoftGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add to Playlist
                IconButton(onClick = onAddToPlaylistClick) {
                    Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = "Add to playlist", tint = SoftGrey, modifier = Modifier.size(18.dp))
                }

                // Download or download progress indicator
                if (isDownloading) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = downloadProgress,
                            color = NeonCoral,
                            strokeWidth = 3.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text("$progressPercent%", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else if (!song.isDownloaded) {
                    IconButton(onClick = onDownloadClick) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Download", tint = SoftGrey, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Play Stream Trigger button (Premium white fill circle with black play arrow)
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .scale(1.1f)
                        .background(Color.White, CircleShape)
                        .size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ==========================================
// NOW PLAYING MINI CONSOLE BOTTOM BAR
// ==========================================

@Composable
fun MiniPlayerBar(
    currentSong: Song,
    isPlaying: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onExpandPlayer: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSlate.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onExpandPlayer() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cover + metadata
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vertical indicator line matching Sophisticated Dark design
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(NeonCyan)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Vintage Vinyl spinning rotation cover
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .rotate(if (isPlaying) rotation else 0f)
                    ) {
                        AsyncImage(
                            model = currentSong.imageUrl,
                            contentDescription = "Spinnin Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = currentSong.title,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong.artist,
                            color = SoftGrey,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePlay) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle play",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onSkipNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Elegant horizontal progress strip
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = progress,
                color = NeonCyan,
                trackColor = Color.White.copy(0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(CircleShape)
            )
        }
    }
}

// ==========================================
// DEEP GRAPHICS FULL SCREEN DISPLAY CONTROLLER
// ==========================================

@Composable
fun FullScreenPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    currentPositionMs: Int,
    durationMs: Int,
    playbackError: String?,
    onMinimize: () -> Unit,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onSeek: (Float) -> Unit,
    onAddToPlaylist: () -> Unit
) {
    val durationString = remember(durationMs) { formatMs(durationMs) }
    val elapsedString = remember(currentPositionMs) { formatMs(currentPositionMs) }

    // Wave Visualizer Amplitude simulations
    val visualizerAmp = remember { Animatable(0.2f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                visualizerAmp.animateTo(
                    targetValue = Random.nextFloat() * (1f - 0.3f) + 0.3f,
                    animationSpec = tween(150, easing = LinearOutSlowInEasing)
                )
            }
        } else {
            visualizerAmp.animateTo(0.15f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Glowing overlay ambient shapes in the background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = NeonCyan.copy(alpha = 0.08f),
                radius = 350f,
                center = Offset(size.width * 0.2f, size.height * 0.4f)
            )
            drawCircle(
                color = NeonCoral.copy(alpha = 0.08f),
                radius = 450f,
                center = Offset(size.width * 0.8f, size.height * 0.7f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMinimize) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("تشغيل الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftGrey, letterSpacing = 2.sp)
                    Text(
                        text = "STUDIO HIGH-RES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                }
                IconButton(onClick = onAddToPlaylist) {
                    Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = "Add Playlist", tint = Color.White)
                }
            }

            // Big Vinyl Record Cover
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Record Outer sleeve frame
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .shadow(24.dp, shape = CircleShape, ambientColor = NeonCyan, spotColor = NeonCyan)
                            .border(6.dp, Brush.radialGradient(listOf(Color.White.copy(0.15f), Color.Transparent)), CircleShape)
                            .padding(8.dp)
                    ) {
                        AsyncImage(
                            model = song.imageUrl,
                            contentDescription = "Large player art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                        // Spinning core notch hole
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp)
                                .background(DeepObsidian, CircleShape)
                                .border(2.dp, Color.White.copy(0.4f), CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = song.title,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        color = SoftGrey,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(NeonCyan.copy(0.2f), NeonCoral.copy(0.2f))))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(song.platform, fontSize = 10.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Studio Soundwave visualization oscilloscope canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                OscilloscopeCanvas(amplitudeMultiplier = visualizerAmp.value)
            }

            // Central Lyrics Drawer Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(0.04f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                ScrollingLyricsView(songTitle = song.title, artistName = song.artist, isPlaying = isPlaying, progress = progress)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seekbar
            Column(modifier = Modifier.fillMaxWidth()) {
                if (playbackError != null) {
                    Text(
                        text = playbackError,
                        color = NeonCoral,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }

                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(elapsedString, fontSize = 11.sp, color = SoftGrey, fontWeight = FontWeight.Bold)
                    Text(durationString, fontSize = 11.sp, color = SoftGrey, fontWeight = FontWeight.Bold)
                }
            }

            // Playback controls console deck
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev skip
                IconButton(onClick = onPrevious) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(30.dp))
                }

                // 10s Rewind
                IconButton(onClick = onSkipBackward) {
                    Icon(imageVector = Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = SoftGrey, modifier = Modifier.size(24.dp))
                }

                // Main circular Play Button
                Box(
                    modifier = Modifier
                        .shadow(12.dp, shape = CircleShape, ambientColor = NeonCyan, spotColor = NeonCyan)
                        .background(Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF00E5FF))), CircleShape)
                        .clickable { onTogglePlay() }
                        .size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/pause big",
                        tint = DeepObsidian,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 10s FastForward
                IconButton(onClick = onSkipForward) {
                    Icon(imageVector = Icons.Default.Forward10, contentDescription = "FastForward 10s", tint = SoftGrey, modifier = Modifier.size(24.dp))
                }

                // Next skip
                IconButton(onClick = onNext) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

@Composable
fun OscilloscopeCanvas(amplitudeMultiplier: Float) {
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = phase0,
        targetValue = phaseEnd,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val points = 60

        // Draw multiple overlapping glowing sine wave paths representing high-fi audio Visualizer
        val colors = listOf(NeonCyan, NeonCoral, GoldAccent)
        colors.forEachIndexed { index, color ->
            val scale = 12f * (index + 1) * amplitudeMultiplier
            val frequencyMultiplier = 0.015f + (index * 0.005f)
            val offsetPhase = phase + (index * 1.5f)

            for (i in 0 until points - 1) {
                val x1 = (width / points) * i
                val y1 = centerY + sin(x1 * frequencyMultiplier + offsetPhase) * scale

                val x2 = (width / points) * (i + 1)
                val y2 = centerY + sin(x2 * frequencyMultiplier + offsetPhase) * scale

                drawLine(
                    color = color.copy(alpha = 0.7f - (index * 0.15f)),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// Global phase limits for static compile metrics
private const val phase0 = 0f
private const val phaseEnd = 23.5619f // (Math.PI * 7.5).toFloat()

@Composable
fun ScrollingLyricsView(songTitle: String, artistName: String, isPlaying: Boolean, progress: Float) {
    // Elegant local high fidelity lyric synchronizer
    val lyrics = listOf(
        "♪ [بداية المقطوعة الموسيقية - Tarab Studio] ♪",
        "مرحباً بك في عالم مايسترو الاحترافي للأنغام..",
        "البوت يبحث الآن في يوتيوب ومختلف المنصات..",
        "تكامل متناسق وجودات لا نهائية للتحميل..",
        "صوت نقي، تصميم جذاب، وذكاء استثنائي..",
        "♪ [عزف منفرد عالي الدقة] ♪",
        "يمكنك البحث وتنزيل أي مسار بنصف اسمه مجرداً..",
        "شكراً لاختيارك مايسترو Maestro Music Elite Player!"
    )

    val activeLyricIdx = remember(progress) {
        val index = (progress * lyrics.size).toInt()
        index.coerceIn(0, lyrics.size - 1)
    }

    Crossfade(targetState = lyrics[activeLyricIdx], animationSpec = tween(300)) { lyricText ->
        Text(
            text = lyricText,
            color = if (isPlaying) NeonCyan else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==========================================
// CONSOLE DIALOG SHEETS
// ==========================================

@Composable
fun PlaylistSongsDialog(
    playlist: PlaylistEntity,
    songs: List<Song>,
    activeSongId: String?,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onSongSelect: (Song) -> Unit,
    onDeleteSongFromPlaylist: (Song) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardSlate)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxHeight(0.7f)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(playlist.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(playlist.description, fontSize = 11.sp, color = SoftGrey)
                    }
                    if (!playlist.isSmart) {
                        IconButton(onClick = onDeletePlaylist) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Delete Playlist", tint = NeonCoral)
                        }
                    }
                }

                Divider(color = Color.White.copy(0.15f), modifier = Modifier.padding(vertical = 4.dp))

                if (songs.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("هذه القائمة فارغة حالياً. ابحث وأضف أغاني!", color = SoftGrey, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(songs) { song ->
                            val isActive = activeSongId == song.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isActive) NeonCyan.copy(0.08f) else Color.Transparent)
                                    .clickable { onSongSelect(song) }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (isActive && isPlaying) Icons.Default.VolumeUp else Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (isActive) NeonCyan else SoftGrey,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(song.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isActive) NeonCyan else Color.White, maxLines = 1)
                                        Text(song.artist, fontSize = 11.sp, color = SoftGrey, maxLines = 1)
                                    }
                                }
                                if (!playlist.isSmart) {
                                    IconButton(
                                        onClick = { onDeleteSongFromPlaylist(song) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = SoftGrey, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق القائمة", color = DeepObsidian, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardSlate)
                .padding(20.dp)
        ) {
            Column {
                Text("أنشئ قائمة تشغيل احترافية", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القائمة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف القائمة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = SoftGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onCreate(name, description) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("إنشاء", color = DeepObsidian, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QualitySelectorDialog(
    song: Song,
    onDismiss: () -> Unit,
    onChooseQuality: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardSlate)
                .padding(20.dp)
        ) {
            Column {
                Text("تحميل بجودات متعددة وصيغ نقية", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("اختر الجودة المطلوبة لـ: ${song.title}", fontSize = 11.sp, color = SoftGrey)
                Spacer(modifier = Modifier.height(16.dp))

                val qualities = listOf(
                    Triple("High Definition (HQ MP3)", "320 kbps", "9.4 MB"),
                    Triple("Standard Quality (SQ MP3)", "192 kbps", "5.8 MB"),
                    Triple("Lite Compression (LQ M4A)", "128 kbps", "3.2 MB")
                )

                qualities.forEach { (title, speed, size) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(0.03f))
                            .clickable { onChooseQuality("$speed ($size)") }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text(speed, fontSize = 11.sp, color = SoftGrey)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(size, fontSize = 10.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إلغاء الأمر", color = SoftGrey, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onChoosePlaylist: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardSlate)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxHeight(0.5f)) {
                Text("إضافة إلى قائمة تشغيل", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                if (playlists.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("لا توجد قوائم مخصصة بعد. قم بانشاء واحدة أولا!", color = SoftGrey, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(0.03f))
                                    .clickable { onChoosePlaylist(playlist.id) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.QueueMusic, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(playlist.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إلغاء الأمر", color = SoftGrey, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// Helper: duration formatter
private fun formatMs(ms: Int): String {
    val sec = (ms / 1000) % 60
    val min = (ms / (1000 * 60)) % 60
    return String.format("%02d:%02d", min, sec)
}

@Composable
fun SpotifyDeveloperPanel(viewModel: MusicViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isConnected by remember { mutableStateOf(false) }
    val logs = remember { mutableStateListOf("System: Ready for Spotify App Remote SDK Integration") }
    var spotifyAppRemote: SpotifyAppRemote? by remember { mutableStateOf(null) }

    var clientId by remember { mutableStateOf("ad0911afa57949bba362003f601876b2") }
    var redirectUri by remember { mutableStateOf("https://com.spotify.android.spotifysdkkotlindemo/callback") }
    var playlistUri by remember { mutableStateOf("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL") }

    var activeTrackName by remember { mutableStateOf("") }
    var activeArtistName by remember { mutableStateOf("") }

    // Register active change listeners on the SDK Mock
    DisposableEffect(Unit) {
        val playListener: (String) -> Unit = { uri ->
            val trackName = when (uri) {
                "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL" -> "Indie Feel Good Mix"
                "spotify:playlist:37i9dQZF1DX7K31D69s4M1" -> "Serene Piano Resonance"
                else -> "Spotify Premium Playlist"
            }
            val artistName = when (uri) {
                "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL" -> "Indie Artists"
                "spotify:playlist:37i9dQZF1DX7K31D69s4M1" -> "Aura Keyboard Ensemble"
                else -> "The Weeknd"
            }

            logs.add("playerApi.play(\"$uri\") invoked!")

            // Trigger actual audio stream through central system so the user hears music in the app!
            val streamUrl = when (uri) {
                "spotify:playlist:37i9dQZF1DX7K31D69s4M1" -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
                else -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            }

            val spotifySong = Song(
                id = "spotify_remote_stream",
                title = trackName,
                artist = artistName,
                album = "Spotify App Remote",
                duration = "3:20",
                imageUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=200",
                platform = "Spotify",
                streamUrl = streamUrl,
                genre = "Premium Remote"
            )

            viewModel.playSong(spotifySong, listOf(spotifySong))
        }

        val connListener: (Boolean) -> Unit = { connected ->
            isConnected = connected
            if (!connected) {
                spotifyAppRemote = null
                activeTrackName = ""
                activeArtistName = ""
            }
        }

        SpotifyAppRemote.onPlayUriListener = playListener
        SpotifyAppRemote.onConnectionStateListener = connListener

        onDispose {
            SpotifyAppRemote.onPlayUriListener = null
            SpotifyAppRemote.onConnectionStateListener = null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1DB954).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothConnected,
                            contentDescription = "Bluetooth Connected",
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Spotify App Remote SDK",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Connect & Control Spotify on Device",
                            fontSize = 11.sp,
                            color = SoftGrey
                        )
                    }
                }

                // Status tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isConnected) Color(0xFF1DB954).copy(alpha = 0.12f)
                            else Color.Red.copy(alpha = 0.12f)
                        )
                        .border(
                            1.dp,
                            if (isConnected) Color(0xFF1DB954).copy(alpha = 0.4f)
                            else Color.Red.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isConnected) "CONNECTED" else "DISCONNECTED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isConnected) Color(0xFF1DB954) else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Credentials Fields (Horizontal Grid layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = clientId,
                    onValueChange = { clientId = it },
                    label = { Text("Client ID", fontSize = 10.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )

                OutlinedTextField(
                    value = redirectUri,
                    onValueChange = { redirectUri = it },
                    label = { Text("Redirect URI", fontSize = 10.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row (Connect / Disconnect)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        logs.add("connectionParams = ConnectionParams.Builder(clientId).setRedirectUri(redirectUri).showAuthView(true).build()")
                        val connectionParams = ConnectionParams.Builder(clientId)
                            .setRedirectUri(redirectUri)
                            .showAuthView(true)
                            .build()

                        logs.add("SpotifyAppRemote.connect(this, connectionParams, ConnectionListener)")
                        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
                            override fun onConnected(appRemote: SpotifyAppRemote) {
                                spotifyAppRemote = appRemote
                                isConnected = true
                                logs.add("Connected! Yay! 🎉")

                                // Subscribe to PlayerState as instructed by Spotify Quickstart
                                logs.add("playerApi.subscribeToPlayerState()")
                                appRemote.playerApi.subscribeToPlayerState().setEventCallback { state ->
                                    val track = state.track
                                    activeTrackName = track.name
                                    activeArtistName = track.artist.name
                                    logs.add("Track update received: ${track.name} by ${track.artist.name}")
                                }
                            }

                            override fun onFailure(throwable: Throwable) {
                                logs.add("Connection Error: ${throwable.localizedMessage}")
                                Toast.makeText(context, throwable.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                        })
                    },
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) Color.White.copy(alpha = 0.15f) else Color.White,
                        contentColor = if (isConnected) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Power, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Connect SDK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        spotifyAppRemote?.let {
                            logs.add("SpotifyAppRemote.disconnect(appRemote)")
                            SpotifyAppRemote.disconnect(it)
                            isConnected = false
                            logs.add("Disconnected gracefully.")
                            Toast.makeText(context, "Disconnected Spotify App Remote", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isConnected,
                    modifier = Modifier.weight(0.9f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.15f),
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                ) {
                    Icon(imageVector = Icons.Default.PowerOff, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Disconnect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Spotify URI Player Field
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.40f))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Play Playlist / Track URI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftGrey
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = playlistUri,
                        onValueChange = { playlistUri = it },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (!isConnected) {
                                Toast.makeText(context, "Please click 'Connect SDK' first to link Spotify App Remote!", Toast.LENGTH_LONG).show()
                                return@IconButton
                            }
                            spotifyAppRemote?.playerApi?.play(playlistUri)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play URI",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Playlist Preset Quick Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { playlistUri = "spotify:playlist:37i9dQZF1DX2sUQwD7tbmL" }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("📻 Indie Feel Good", fontSize = 10.sp, color = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { playlistUri = "spotify:playlist:37i9dQZF1DX7K31D69s4M1" }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text("🎹 Serene Piano", fontSize = 10.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Code Log Window (Developer Console log)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Realtime SDK Monitor",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Text(
                        text = "CONSOLE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = SoftGrey
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable log text output
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true
                    ) {
                        items(logs.reversed()) { log ->
                            Text(
                                text = "• $log",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
