package org.wit.audioplayer.ui.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wit.audioplayer.R

class MainActivity : AppCompatActivity() {

    private val songLibraryViewModel: SongLibraryViewModel by viewModel()

    private lateinit var recyclerView: RecyclerView
    private val adapter = AudioTrackAdapter()

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
        setContentView(R.layout.activity_main)

        openDocumentTreeLauncher.launch(null)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            songLibraryViewModel.uiState.collect { tracks ->
                adapter.submitList(tracks)
            }
        }
    }
}
