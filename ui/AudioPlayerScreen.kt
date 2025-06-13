// AudioPlayerScreen.kt
package org.wit.audioplayer.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    viewModel: AudioPlayerViewModel = viewModel(),
    onScanClick: () -> Unit
) {
    val tracks by viewModel.tracks.collectAsState()
    val currentTrackId by viewModel.currentTrackId.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()

    val currentTrack = tracks.find { it.id == currentTrackId }
    var currentPosition by remember { mutableStateOf(0L) }

    LaunchedEffect(currentTrackId, playbackState) {
        if (playbackState == Player.STATE_READY) {
            repeat(Int.MAX_VALUE) {
                currentPosition = viewModel.getCurrentPosition()
                delay(500)
                if (playbackState != Player.STATE_READY) return@LaunchedEffect
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Audio Player") },
                actions = {
                    IconButton(onClick = onScanClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan Audio File"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!isScanning && currentTrack != null) {
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
                        currentTrack = currentTrack,
                        repeatMode = repeatMode,
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
                        },
                        onRepeatModeChange = { viewModel.toggleRepeatMode() }
                    )
                }
            }
        }
    ) { padding ->
        if (isScanning) {
            ScanningState(
                modifier = Modifier.padding(padding),
                progress = scanProgress
            )
        } else {
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
}
