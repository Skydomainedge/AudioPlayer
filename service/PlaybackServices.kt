package org.wit.audioplayer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.wit.audioplayer.data.local.entity.AudioTrack

/**
 * Service to manage music playback using ExoPlayer.
 * It exposes flows for current track and playback state,
 * and provides controls for play, pause, and stop.
 */
class PlaybackServices : Service() {

    private val binder = LocalBinder()
    private lateinit var exoPlayer: ExoPlayer

    // StateFlow to represent current playing track (null if none)
    private val _currentTrackFlow = MutableStateFlow<AudioTrack?>(null)
    val currentTrackFlow: StateFlow<AudioTrack?> = _currentTrackFlow

    // StateFlow to represent whether playback is active
    private val _isPlayingFlow = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow

    override fun onCreate() {
        super.onCreate()
        // Initialize ExoPlayer instance
        exoPlayer = ExoPlayer.Builder(this).build()

        // Listen for player state changes to update isPlayingFlow
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlayingFlow.value = isPlaying
                if (!isPlaying && exoPlayer.playbackState == Player.STATE_ENDED) {
                    // Playback ended, reset current track
                    _currentTrackFlow.value = null
                    stopForeground(true)
                }
            }
        })

        // Create notification channel for Android O+
        createNotificationChannel()
    }

    /**
     * Binder class for clients to interact with this service.
     */
    inner class LocalBinder : Binder() {
        fun getService(): PlaybackServices = this@PlaybackServices
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    /**
     * Plays the given AudioTrack.
     * Prepares ExoPlayer with the media URI and starts playback.
     * @param track The AudioTrack to play.
     */
    fun playTrack(track: AudioTrack) {
        val mediaItem = MediaItem.fromUri(track.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _currentTrackFlow.value = track

        // Start foreground service with notification
        startForeground(1, buildNotification(track))
    }

    /**
     * Toggles playback between play and pause states.
     */
    fun play() {
        if (!exoPlayer.isPlaying) {
            exoPlayer.play()
        }
    }

    fun pause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    /**
     * Stops playback, releases resources, and stops the service.
     */
    fun stopPlayback() {
        exoPlayer.stop()
        exoPlayer.release()
        _currentTrackFlow.value = null
        _isPlayingFlow.value = false
        stopForeground(true)
        stopSelf()
    }

    /**
     * Builds a simple notification showing the current track playing.
     */
    private fun buildNotification(track: AudioTrack): Notification {
        return NotificationCompat.Builder(this, "audio_playback_channel")
            .setContentTitle(track.title)
            .setContentText(track.artist ?: "Unknown Artist")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    /**
     * Creates notification channel for Android O and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "audio_playback_channel",
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}
