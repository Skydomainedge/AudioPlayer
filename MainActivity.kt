package org.wit.audioplayer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.documentfile.provider.DocumentFile
import org.wit.audioplayer.ui.AudioPlayerScreen
import org.wit.audioplayer.ui.AudioPlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel by lazy { AudioPlayerViewModel(application) }

    // 目录选择器
    private val directoryPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            handleSelectedDirectory(uri)
        } else {
            showErrorMessage("未选择目录")
        }
    }

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
        // 直接启动目录选择器，不进行版本检查
        pickDirectory()
    }

    private fun handleSelectedDirectory(uri: Uri) {
        try {
            // 获取持久化权限
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            // 尝试获取持久化权限
            try {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                // 检查是否真的获取了权限
                val hasPermission = contentResolver.persistedUriPermissions.any {
                    it.uri == uri && it.isReadPermission
                }
                if (!hasPermission) {
                    showErrorMessage("无法获取持久化权限")
                    return
                }
            } catch (e: Exception) {
                showErrorMessage("获取持久化权限失败: ${e.message}")
                return
            }

            // 验证目录可访问性
            if (DocumentFile.fromTreeUri(this, uri)?.canRead() == true) {
                viewModel.scanCustomDirectory(contentResolver, uri)
            } else {
                showErrorMessage("无法读取目录内容")
            }
        } catch (e: Exception) {
            showErrorMessage("目录访问错误: ${e.localizedMessage ?: "未知错误"}")
        }
    }

    private fun pickDirectory() {
        // 不指定初始URI以获得更好的兼容性
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