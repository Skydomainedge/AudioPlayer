package org.wit.audioplayer.ui.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.wit.audioplayer.data.local.AppDatabase
import org.wit.audioplayer.data.repository.AudioRepository
import org.wit.audioplayer.di.ViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var songLibraryViewModel: SongLibraryViewModel

    private val openDocumentTreeLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            lifecycleScope.launch {
                songLibraryViewModel.refreshLibrary(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        val dao = database.audioTrackDao()
        val repository = AudioRepository(applicationContext, dao)
        val factory = ViewModelFactory(repository)

        songLibraryViewModel = ViewModelProvider(this, factory)[SongLibraryViewModel::class.java]

        openDocumentTreeLauncher.launch(null)

        lifecycleScope.launch {
            songLibraryViewModel.uiState.collect { tracks ->
                // TODO: refresh list
            }
        }
    }
}
