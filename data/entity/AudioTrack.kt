package org.wit.audioplayer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "audio_tracks")
data class AudioTrack(
    @PrimaryKey(autoGenerate = false) // 使用媒体库ID作为主键
    @ColumnInfo(name = "track_id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "uri")
    val uri: String,

    @ColumnInfo(name = "duration") // 音频时长(毫秒)
    val duration: Long = 0,

    @ColumnInfo(name = "artist")
    val artist: String? = null,

    @ColumnInfo(name = "album")
    val album: String? = null,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis()
)