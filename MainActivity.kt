package org.wit.audioplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import org.wit.audioplayer.ui.AudioPlayerScreen
import org.wit.audioplayer.ui.AudioPlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel by lazy { AudioPlayerViewModel(application) }

    // 用于请求存储权限
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pickDirectory() // Android 10+ 使用SAF
            } else {
                scanAudio() // 旧版本使用传统方式
            }
        }
    }

    // 用于选择目录
    private val directoryPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // 获取持久化权限
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
            viewModel.scanCustomDirectory(contentResolver, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    AudioPlayerScreen(
                        viewModel = viewModel,
                        onScanClick = { checkPermissionAndScan() }
                    )
                }
            }
        }
    }

    private fun checkPermissionAndScan() {
        when {
            // 检查是否已有权限
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    pickDirectory()
                } else {
                    scanAudio()
                }
            }
            // Android 13+ 需要不同的权限
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun scanAudio() {
        viewModel.scanAudioFiles(contentResolver)
    }

    private fun pickDirectory() {
        // 可选：设置初始目录（某些设备可能不支持）
        val initialUri = Uri.parse("content://com.android.externalstorage.documents/root/primary")
        directoryPickerLauncher.launch(initialUri)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.exoPlayer.release()
    }
}