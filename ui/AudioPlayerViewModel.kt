package org.wit.audioplayer.ui

import android.app.Application
import android.content.ContentResolver
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
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

class AudioPlayerViewModel(application: Application) : AndroidViewModel(application), Player.Listener {

    private val db = AppDatabase.getDatabase(application)
    private val audioTrackDao = db.audioTrackDao()

    private val _exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        addListener(this@AudioPlayerViewModel)
        playWhenReady = false
        repeatMode = Player.REPEAT_MODE_OFF
    }

    // 对外暴露只读的播放器引用（可选）
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
        _exoPlayer.stop()
        _exoPlayer.setMediaSource(createMediaSource(Uri.parse(track.uri)))
        _exoPlayer.prepare()
        _exoPlayer.playWhenReady = true
        _currentTrackId.value = track.id
    }

    fun togglePlayPause() {
        when {
            _exoPlayer.isPlaying -> {
                _exoPlayer.pause()
            }
            _exoPlayer.playbackState == Player.STATE_READY -> {
                _exoPlayer.play()
            }
            currentTrackId.value != null -> {
                tracks.value.find { it.id == currentTrackId.value }?.let { playTrack(it) }
            }
        }
    }

    fun stop() {
        _exoPlayer.stop()
        _currentTrackId.value = null
    }

    fun seekTo(position: Long) {
        _exoPlayer.seekTo(position)
    }

    fun getCurrentPosition(): Long = _exoPlayer.currentPosition

    fun getDuration(): Long = _exoPlayer.duration

    override fun onPlaybackStateChanged(playbackState: Int) {
        _playbackState.value = playbackState
        if (playbackState == Player.STATE_ENDED) {
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

    fun scanCustomDirectory(contentResolver: ContentResolver, treeUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val audioList = mutableListOf<AudioTrack>()
            val rootDocument = DocumentFile.fromTreeUri(getApplication(), treeUri)

            rootDocument?.let { scanDirectory(it, audioList) }

            audioTrackDao.clearAll()
            audioTrackDao.insertTracks(audioList)
            _tracks.value = audioList
        }
    }

    private fun scanDirectory(directory: DocumentFile, result: MutableList<AudioTrack>) {
        directory.listFiles().forEach { file ->
            when {
                file.isDirectory -> scanDirectory(file, result)
                isAudioFile(file) -> result.add(createAudioTrack(file))
            }
        }
    }

    private fun isAudioFile(file: DocumentFile): Boolean {
        return when (file.type?.lowercase()) {
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/aac", "audio/ogg" -> true
            else -> file.name?.let { name ->
                name.endsWith(".mp3", ignoreCase = true) ||
                        name.endsWith(".wav", ignoreCase = true) ||
                        name.endsWith(".aac", ignoreCase = true) ||
                        name.endsWith(".ogg", ignoreCase = true)
            } ?: false
        }
    }

    private fun createAudioTrack(file: DocumentFile): AudioTrack {
        return AudioTrack(
            id = (file.uri.toString() + file.name).hashCode().toLong(),
            title = file.name ?: "Unknown",
            uri = file.uri.toString(),
            duration = getAudioDuration(file.uri),
            artist = null,
            album = null
        )
    }

    private fun getAudioDuration(uri: Uri): Long {
        return try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(getApplication(), uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun scanAudioFiles(contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            val audioList = mutableListOf<AudioTrack>()

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM
            )

            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                while (cursor.moveToNext()) {
                    audioList.add(
                        AudioTrack(
                            id = cursor.getLong(idIndex),
                            title = cursor.getString(titleIndex) ?: "Unknown",
                            uri = Uri.withAppendedPath(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                cursor.getLong(idIndex).toString()
                            ).toString(),
                            duration = cursor.getLong(durationIndex),
                            artist = cursor.getString(artistIndex),
                            album = cursor.getString(albumIndex)
                        )
                    )
                }
            }

            audioTrackDao.clearAll()
            audioTrackDao.insertTracks(audioList)
            _tracks.value = audioList
        }
    }
}