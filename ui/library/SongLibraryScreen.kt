package org.wit.audioplayer.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.wit.audioplayer.data.local.entity.AudioTrack

/**
 * Composable function that displays a list of songs.
 * It receives a list of audio tracks and a click listener.
 */
@Composable
fun SongLibraryScreen(
    tracks: List<AudioTrack>,
    onTrackClick: (AudioTrack) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Song Library",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display a message if no tracks are available
        if (tracks.isEmpty()) {
            Text("No songs found. Please scan your music library.")
        } else {
            // Display song list using LazyColumn
            LazyColumn {
                items(tracks) { track ->
                    SongListItem(track = track, onClick = { onTrackClick(track) })
                    Divider()
                }
            }
        }
    }
}
