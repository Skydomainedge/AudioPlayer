package org.wit.audioplayer.data.model

import androidx.annotation.IntDef

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val currentTrackId: Long? = null,
    @PlaybackMode
    val playbackMode: Int = PlaybackMode.SEQUENTIAL
)


@IntDef(PlaybackMode.SEQUENTIAL, PlaybackMode.REPEAT_ONE, PlaybackMode.SHUFFLE)
@Retention(AnnotationRetention.SOURCE)
annotation class PlaybackMode {
    companion object {
        const val SEQUENTIAL = 0
        const val REPEAT_ONE = 1
        const val SHUFFLE = 2
    }
}
