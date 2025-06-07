package org.wit.audioplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.wit.audioplayer.ui.library.SongLibraryScreen
import org.wit.audioplayer.ui.player.PlayerScreen

/**
 * Main navigation graph for the app.
 * Defines routes for the song library and player screens.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            // Placeholder for now; pass real viewmodel or track list here
            SongLibraryScreen(tracks = listOf(), onTrackClick = { track ->
                navController.navigate("player")
            })
        }
        composable("player") {
            // Placeholder for now; real playback info should be passed via ViewModel
            PlayerScreen(title = "Song Title", artist = "Artist", isPlaying = false, onPlayPauseToggle = {})
        }
    }
}
