package org.wit.audioplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_tracks")
data class AudioTrack(
    @PrimaryKey val uri: String,      // Unique URI as primary key
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,               // Duration in milliseconds
    val trackNumber: Int? = null
)
