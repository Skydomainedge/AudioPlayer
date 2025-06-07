package org.wit.audioplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.wit.audioplayer.data.local.entity.AudioTrack

@Database(
    entities = [AudioTrack::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioTrackDao(): AudioTrackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "audio_player_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
