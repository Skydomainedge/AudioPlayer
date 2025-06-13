package org.wit.audioplayer.data.dao

import androidx.room.*
import org.wit.audioplayer.data.entity.AudioTrack

@Dao
interface AudioTrackDao {

    @Query("SELECT * FROM audio_tracks ORDER BY title ASC")
    fun getAllTracks(): List<AudioTrack>

    @Query("SELECT * FROM audio_tracks WHERE title LIKE :query OR artist LIKE :query ORDER BY title ASC")
    fun searchTracks(query: String): List<AudioTrack>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: AudioTrack): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<AudioTrack>)

    @Query("DELETE FROM audio_tracks")
    suspend fun clearAll()

    @Delete
    suspend fun deleteTrack(track: AudioTrack): Int

    @Query("DELETE FROM audio_tracks WHERE track_id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM audio_tracks WHERE track_id = :id")
    suspend fun findById(id: Long): AudioTrack?

    @Query("SELECT COUNT(*) FROM audio_tracks")
    suspend fun getCount(): Int
}