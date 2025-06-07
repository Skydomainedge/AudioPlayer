package org.wit.audioplayer.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.wit.audioplayer.data.local.entity.AudioTrack

@Composable
fun SongList(
    tracks: List<AudioTrack>,
    onTrackClick: (AudioTrack) -> Unit
) {
    LazyColumn {
        items(tracks) { track ->
            Column(
                modifier = Modifier
                    .clickable { onTrackClick(track) }
                    .padding(16.dp)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = track.artist ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Divider()
        }
    }
}
