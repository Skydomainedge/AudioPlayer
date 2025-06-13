package org.wit.audioplayer.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import org.wit.audioplayer.data.entity.AudioTrack

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun TrackList(
    tracks: List<AudioTrack>,
    currentTrackId: Long?,
    playbackState: Int,
    onPlayClick: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        items(tracks) { track ->
            val isCurrentTrack = track.id == currentTrackId
            val isPlaying = isCurrentTrack && playbackState == Player.STATE_READY

            TrackListItem(
                track = track,
                isPlaying = isPlaying,
                isCurrentTrack = isCurrentTrack,
                onClick = { onPlayClick(track) }
            )

        }
    }
}