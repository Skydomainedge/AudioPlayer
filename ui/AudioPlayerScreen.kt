// AudioPlayerScreen.kt
package org.wit.audioplayer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    viewModel: AudioPlayerViewModel = viewModel(),
    onScanClick: () -> Unit
) {
    val tracks by viewModel.tracks.collectAsState()
    val currentTrackId by viewModel.currentTrackId.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTrack = tracks.find { it.id == currentTrackId }

    // Add state for current position
    var currentPosition by remember { mutableStateOf(0L) }

    // Update position periodically
    LaunchedEffect(currentTrackId, playbackState) {
        while (true) {
            if (playbackState == Player.STATE_READY) {
                currentPosition = viewModel.getCurrentPosition()
            }
            delay(500) // Update every 500ms
        }
    }

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
        },
        bottomBar = {
            if (currentTrack != null) {
                Column {
                    TrackProgressBar(
                        currentPosition = currentPosition,
                        duration = currentTrack.duration,
                        onSeekChanged = { position ->
                            viewModel.seekTo(position)
                            currentPosition = position
                        }
                    )

                    PlayerControls(
                        isPlaying = playbackState == Player.STATE_READY && viewModel.exoPlayer.isPlaying,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onPreviousClick = {
                            val currentIndex = tracks.indexOfFirst { it.id == currentTrackId }
                            if (currentIndex > 0) {
                                viewModel.playTrack(tracks[currentIndex - 1])
                            }
                        },
                        onNextClick = {
                            val currentIndex = tracks.indexOfFirst { it.id == currentTrackId }
                            if (currentIndex < tracks.size - 1) {
                                viewModel.playTrack(tracks[currentIndex + 1])
                            }
                        }
                    )
                }
            }
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