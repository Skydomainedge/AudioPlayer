package org.wit.audioplayer.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable screen for displaying and controlling music playback.
 * Shows current song and play/pause buttons.
 */
@Composable
fun PlayerScreen(
    title: String,
    artist: String?,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(text = artist ?: "Unknown Artist", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // Play/Pause button
        Button(onClick = onPlayPauseToggle) {
            Text(text = if (isPlaying) "Pause" else "Play")
        }
    }
}
