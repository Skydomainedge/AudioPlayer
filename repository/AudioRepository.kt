package org.wit.audioplayer.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.wit.audioplayer.data.local.AudioTrackDao
import org.wit.audioplayer.data.local.entity.AudioTrack
import org.wit.audioplayer.utils.FileUtils

/**
 * Repository class for managing audio track data operations.
 * Acts as an abstraction layer between the data source (Room DB) and the ViewModel.
 */
class AudioRepository(
    private val context: Context,
    private val audioTrackDao: AudioTrackDao
) {

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

    /**
     * Scans the directory Uri (SAF) for audio files and saves them to the database.
     *
     * @param treeUri Uri of the root folder to scan
     */
    suspend fun scanAndSaveTracks(treeUri: Uri) {
        withContext(Dispatchers.IO) {
            // Clear existing data first (optional, depends on your logic)
            audioTrackDao.clearAll()

            // Scan files with FileUtils
            val scannedTracks = FileUtils.scanAudioFiles(context, treeUri)

            // Insert scanned tracks into DB
            audioTrackDao.insertTracks(scannedTracks)
        }
    }
}
