package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.playback.PlaybackManager
import com.example.ui.MusicViewModel
import com.example.ui.screens.MainMusicScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full Edge-to-Edge display support for modern immersive look
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme(darkTheme = true) { // Force premium dark studio look
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainMusicScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release audio player resources gracefully
        PlaybackManager.cleanUp()
    }
}
