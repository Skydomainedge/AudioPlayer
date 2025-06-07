package org.wit.audioplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.wit.audioplayer.data.local.entity.AudioTrack

@Database(
    entities = [AudioTrack::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioTrackDao(): AudioTrackDao
}
