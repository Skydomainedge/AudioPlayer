package org.wit.audioplayer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.documentfile.provider.DocumentFile
import org.wit.audioplayer.ui.AudioPlayerScreen
import org.wit.audioplayer.ui.AudioPlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel by lazy { AudioPlayerViewModel(application) }

    // Directory picker launcher
    private val directoryPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            handleSelectedDirectory(uri)
        } else {
            showErrorMessage("No directory selected")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    AudioPlayerScreen(
                        viewModel = viewModel,
                        onScanClick = { startFileAccessFlow() }
                    )
                }
            }
        }
    }

    private fun startFileAccessFlow() {
        // Directly start the directory picker without version check
        pickDirectory()
    }

    private fun handleSelectedDirectory(uri: Uri) {
        try {
            // Flags for persistent URI permission
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            // Attempt to take persistable permission
            try {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                // Verify if permission was successfully granted
                val hasPermission = contentResolver.persistedUriPermissions.any {
                    it.uri == uri && it.isReadPermission
                }
                if (!hasPermission) {
                    showErrorMessage("Unable to obtain persistent permission")
                    return
                }
            } catch (e: Exception) {
                showErrorMessage("Failed to obtain persistent permission: ${e.message}")
                return
            }

            // Verify directory accessibility
            if (DocumentFile.fromTreeUri(this, uri)?.canRead() == true) {
                viewModel.scanCustomDirectory(uri)
            } else {
                showErrorMessage("Unable to read directory contents")
            }
        } catch (e: Exception) {
            showErrorMessage("Directory access error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun pickDirectory() {
        // Launch directory picker without specifying initial URI for better compatibility
        directoryPickerLauncher.launch(null)
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.exoPlayer.release()
    }
}
