package org.wit.audioplayer.data.repository

import kotlinx.coroutines.flow.Flow
import org.wit.audioplayer.data.local.AudioTrackDao
import org.wit.audioplayer.data.local.entity.AudioTrack

/**
 * Repository class for managing audio track data operations.
 * Acts as an abstraction layer between the data source (Room DB) and the ViewModel.
 */
class AudioRepository(private val audioTrackDao: AudioTrackDao) {

    /**
     * Fetches all audio tracks from the database as a Flow stream.
     */
    fun getAllTracks(): Flow<List<AudioTrack>> {
        return audioTrackDao.getAllTracks()
    }


    suspend fun insertTrack(track: AudioTrack) {
        audioTrackDao.insertTrack(track)
    }


    suspend fun insertTracks(tracks: List<AudioTrack>) {
        audioTrackDao.insertTracks(tracks)
    }

    suspend fun deleteTrack(track: AudioTrack) {
        audioTrackDao.deleteTrack(track)
    }

    suspend fun clearAllTracks() {
        audioTrackDao.clearAll()
    }
}
