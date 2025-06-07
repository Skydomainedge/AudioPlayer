package org.wit.audioplayer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * A foreground service responsible for managing audio playback using ExoPlayer.
 * Handles player initialization, playback control, and notification updates.
 */
class PlaybackServices : Service() {

    private val binder = LocalBinder()
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        // Initialize ExoPlayer instance
        exoPlayer = ExoPlayer.Builder(this).build()
        // Create notification channel for Android O+
        createNotificationChannel()
    }

    /**
     * Binds the service to allow client components to interact with the player.
     */
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    /**
     * Local binder class to expose service methods to clients.
     */
    inner class LocalBinder : Binder() {
        fun getService(): PlaybackServices = this@PlaybackServices
    }

    /**
     * Prepares the player with the given media URI and starts playback.
     * @param uri The URI of the audio media to play.
     */
    fun playMedia(uri: String) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        // Start foreground with a simple notification
        startForeground(1, buildNotification())
    }

    /**
     * Stops playback and releases player resources.
     */
    fun stopPlayback() {
        exoPlayer.stop()
        exoPlayer.release()
        stopForeground(true)
        stopSelf()
    }

    /**
     * Builds a notification for the foreground service.
     */
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "audio_playback_channel")
            .setContentTitle("Playing Audio")
            .setContentText("Your music is playing")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    /**
     * Creates a notification channel required for foreground service on Android O+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "audio_playback_channel",
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
