package org.wit.audioplayer.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.wit.audioplayer.data.repository.AudioRepository
import org.wit.audioplayer.ui.library.SongLibraryViewModel

class ViewModelFactory(
    private val repository: AudioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongLibraryViewModel::class.java)) {
            return SongLibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
