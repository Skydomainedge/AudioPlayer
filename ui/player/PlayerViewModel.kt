package org.wit.audioplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.wit.audioplayer.data.local.entity.AudioTrack
import org.wit.audioplayer.service.PlaybackServices

/**
 * ViewModel responsible for managing music playback state and controls.
 * It interacts with the playback service to play, pause, and track current playing song.
 */
class PlayerViewModel(
    private val playbackService: PlaybackServices
) : ViewModel() {

    // Flow representing the current playing track
    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    // Flow representing whether the player is currently playing
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        // Observe playback service state updates
        viewModelScope.launch {
            playbackService.currentTrackFlow.collect {
                _currentTrack.value = it
            }
        }
        viewModelScope.launch {
            playbackService.isPlayingFlow.collect {
                _isPlaying.value = it
            }
        }
    }

    /**
     * Toggles playback state between play and pause.
     */
    fun togglePlayPause() {
        viewModelScope.launch {
            if (_isPlaying.value) {
                playbackService.pause()
            } else {
                playbackService.play()
            }
        }
    }

    /**
     * Starts playing the specified track.
     */
    fun playTrack(track: AudioTrack) {
        viewModelScope.launch {
            playbackService.playTrack(track)
            _currentTrack.value = track
        }
    }
}
