package org.wit.audioplayer.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.wit.audioplayer.data.local.entity.AudioTrack
import org.wit.audioplayer.data.repository.AudioRepository

/**
 * ViewModel responsible for managing the state of the song library screen.
 * It fetches the list of audio tracks from the repository and exposes them as a StateFlow.
 */
class SongLibraryViewModel(
    private val repository: AudioRepository
) : ViewModel() {

    // Backing property for the UI state flow containing the list of tracks
    private val _uiState = MutableStateFlow<List<AudioTrack>>(emptyList())
    val uiState: StateFlow<List<AudioTrack>> = _uiState.asStateFlow()

    init {
        // Load the list of audio tracks when ViewModel is created
        viewModelScope.launch {
            repository.getAllTracks()
                .collect { tracks ->
                    _uiState.value = tracks
                }
        }
    }

    fun refreshLibrary(treeUri: Uri) {
        viewModelScope.launch {
            repository.scanAndSaveTracks(treeUri)
            val updatedTracks = repository.getAllTracks().first()
            _uiState.value = updatedTracks
        }
    }

}
