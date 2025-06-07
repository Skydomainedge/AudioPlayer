package org.wit.audioplayer.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.wit.audioplayer.data.local.entity.AudioTrack

/**
 * Displays a single audio track item.
 * Includes title, artist, and a clickable area to trigger playback.
 */
@Composable
fun SongListItem(
    track: AudioTrack,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(text = track.title, style = MaterialTheme.typography.titleMedium)
        Text(text = track.artist ?: "Unknown Artist", style = MaterialTheme.typography.bodyMedium)
    }
}
