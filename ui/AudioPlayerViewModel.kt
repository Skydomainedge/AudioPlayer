package org.wit.audioplayer.ui

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.OptIn
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.MediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wit.audioplayer.data.AppDatabase
import org.wit.audioplayer.data.entity.AudioTrack
import androidx.core.net.toUri

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application), Player.Listener {

    private val db = AppDatabase.getDatabase(application)
    private val audioTrackDao = db.audioTrackDao()
    private val _isScanning = MutableStateFlow(false)
    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress
    val isScanning: StateFlow<Boolean> = _isScanning
    private var mediaSources: List<MediaSource> = emptyList()

    private val _exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        addListener(this@AudioPlayerViewModel)
        playWhenReady = false
        repeatMode = Player.REPEAT_MODE_ALL
    }

    val exoPlayer: ExoPlayer
        get() = _exoPlayer

    private val dataSourceFactory = DefaultDataSource.Factory(application)

    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks: StateFlow<List<AudioTrack>> = _tracks

    private val _currentTrackId = MutableStateFlow<Long?>(null)
    val currentTrackId: StateFlow<Long?> = _currentTrackId

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState

    init {
        loadTracks()
    }

    private val _repeatMode = MutableStateFlow(_exoPlayer.repeatMode)
    val repeatMode: StateFlow<Int> = _repeatMode

    fun toggleRepeatMode() {
        _exoPlayer.repeatMode = when (_exoPlayer.repeatMode) {
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_ALL
        }
        _repeatMode.value = _exoPlayer.repeatMode
    }

    private fun loadTracks() {
        viewModelScope.launch(Dispatchers.IO) {
            val trackList = audioTrackDao.getAllTracks()
            _tracks.value = trackList
        }
    }

    @OptIn(UnstableApi::class)
    private fun createMediaSource(uri: Uri): MediaSource {
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
    }

    @OptIn(UnstableApi::class)
    fun playTrack(track: AudioTrack) {
        if (mediaSources.isEmpty() || mediaSources.size != _tracks.value.size) {
            mediaSources = _tracks.value.map { createMediaSource(it.uri.toUri()) }
            _exoPlayer.setMediaSources(mediaSources)
            _exoPlayer.prepare()
        }

        val index = _tracks.value.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            _exoPlayer.seekTo(index, 0L)
            _exoPlayer.playWhenReady = true
            _currentTrackId.value = track.id
        }
    }

    fun togglePlayPause() {
        when {
            _exoPlayer.isPlaying -> _exoPlayer.pause()
            _exoPlayer.playbackState == Player.STATE_READY -> _exoPlayer.play()
            currentTrackId.value != null -> {
                tracks.value.find { it.id == currentTrackId.value }?.let { playTrack(it) }
            }
            _tracks.value.isNotEmpty() -> playTrack(_tracks.value.first())
        }
    }

    fun seekTo(position: Long) {
        _exoPlayer.seekTo(position)
    }

    fun getCurrentPosition(): Long = _exoPlayer.currentPosition

    override fun onPlaybackStateChanged(playbackState: Int) {
        _playbackState.value = playbackState
        if (playbackState == Player.STATE_ENDED && _exoPlayer.repeatMode == Player.REPEAT_MODE_OFF) {
            _currentTrackId.value = null
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        _currentTrackId.value = null
        _playbackState.value = Player.STATE_IDLE
    }

    override fun onCleared() {
        super.onCleared()
        _exoPlayer.removeListener(this)
        _exoPlayer.release()
    }

    fun scanCustomDirectory(treeUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true  // Start scanning
            try {
                val rootDocument = DocumentFile.fromTreeUri(getApplication(), treeUri)
                    ?: throw SecurityException("Unable to resolve tree URI")

                val totalFiles = countFilesRecursively(rootDocument)

                val audioList = mutableListOf<AudioTrack>()
                var scannedFiles = 0

                fun scanDirectoryWithProgress(directory: DocumentFile) {
                    val files = directory.listFiles()
                    if (files.isEmpty()) return

                    for (file in files) {
                        if (file.isDirectory) {
                            scanDirectoryWithProgress(file)
                        } else if (isAudioFile(file)) {
                            val track = createAudioTrack(file)
                            if (track.uri.isNotEmpty()) {
                                audioList.add(track)
                            }
                        }
                        scannedFiles++
                        val progress = if (totalFiles > 0) scannedFiles.toFloat() / totalFiles else 1f
                        _scanProgress.value = progress.coerceIn(0f, 1f)
                    }
                }

                scanDirectoryWithProgress(rootDocument)

                audioTrackDao.clearAll()
                audioTrackDao.insertTracks(audioList)
                _tracks.value = audioTrackDao.getAllTracks()
            } catch (e: Exception) {
                _tracks.value = emptyList()
            } finally {
                _isScanning.value = false
                _scanProgress.value = 0f  // Reset after scan
            }
        }
    }

    private fun countFilesRecursively(directory: DocumentFile): Int {
        var count = 0
        val files = directory.listFiles()
        for (file in files) {
            count += if (file.isDirectory) countFilesRecursively(file) else 1
        }
        return count
    }

    private fun isAudioFile(file: DocumentFile): Boolean {
        return when (file.type?.lowercase()) {
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/aac", "audio/ogg" -> true
            else -> file.name?.let {
                it.endsWith(".mp3", true) || it.endsWith(".wav", true) ||
                        it.endsWith(".aac", true) || it.endsWith(".ogg", true)
            } ?: false
        }
    }

    private fun createAudioTrack(file: DocumentFile): AudioTrack {
        return try {
            val uri = file.uri?.toString() ?: ""
            if (uri.isEmpty()) throw IllegalArgumentException("Invalid file URI")

            val retriever = MediaMetadataRetriever().apply {
                setDataSource(getApplication(), uri.toUri())
            }

            var title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            var artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

            if ((title.isNullOrBlank() || title.startsWith("AUD")) && artist.isNullOrBlank()) {
                val fileName = file.name?.substringBeforeLast('.') ?: "Unknown - Unknown"
                val parts = fileName.split(" - ", "—", "–", "-", limit = 2).map { it.trim() }
                if (parts.size == 2) {
                    artist = parts[0].ifEmpty { "Unknown Artist" }
                    title = parts[1].ifEmpty { "Unknown Title" }
                } else {
                    artist = "Unknown Artist"
                    title = fileName
                }
            }

            retriever.release()

            AudioTrack(
                id = (uri + file.name).hashCode().toLong(),
                title = title ?: "Unknown Title",
                uri = uri,
                duration = duration,
                artist = artist ?: "Unknown Artist",
                album = album
            )
        } catch (e: Exception) {
            AudioTrack(
                id = 0,
                title = "Invalid Track",
                uri = "",
                duration = 0,
                artist = "Unknown Artist",
                album = "Unknown Album"
            )
        }
    }
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val index = _exoPlayer.currentMediaItemIndex
        val track = _tracks.value.getOrNull(index)
        _currentTrackId.value = track?.id
    }
}
