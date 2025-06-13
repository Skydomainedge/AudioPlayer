package org.wit.audioplayer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    viewModel: AudioPlayerViewModel = viewModel(),
    onScanClick: () -> Unit
) {
    val tracks by viewModel.tracks.collectAsState()
    val currentTrackId by viewModel.currentTrackId.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("音频播放器") },
                actions = {
                    IconButton(onClick = onScanClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "扫描音乐"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (tracks.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            TrackList(
                tracks = tracks,
                currentTrackId = currentTrackId,
                playbackState = playbackState,
                onPlayClick = { track ->
                    if (track.id == currentTrackId && playbackState == Player.STATE_READY) {
                        viewModel.togglePlayPause()
                    } else {
                        viewModel.playTrack(track)
                    }
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}