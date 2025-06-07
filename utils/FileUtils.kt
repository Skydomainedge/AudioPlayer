package org.wit.audioplayer.utils

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.wit.audioplayer.data.local.entity.AudioTrack

object FileUtils {

    /**
     * Recursively scans the given directory Uri for MP3 files,
     * retrieves metadata, and returns a list of AudioTrack objects.
     *
     * @param context Context used to access ContentResolver
     * @param treeUri Uri representing the directory selected via SAF
     * @return List of AudioTrack containing metadata for each MP3 found
     */
    fun scanMp3FilesInDirectory(context: Context, treeUri: Uri): List<AudioTrack> {
        val tracks = mutableListOf<AudioTrack>()

        // Wrap Uri with DocumentFile for SAF traversal
        val directory = DocumentFile.fromTreeUri(context, treeUri)
            ?: return emptyList()

        // Recursive helper function to traverse files
        fun traverseFiles(docFile: DocumentFile) {
            if (docFile.isDirectory) {
                // If directory, recursively scan children
                docFile.listFiles().forEach { traverseFiles(it) }
            } else if (docFile.isFile && docFile.name?.endsWith(".mp3", ignoreCase = true) == true) {
                // If MP3 file, extract metadata and add to list
                val metadata = retrieveMetadata(context.contentResolver, docFile.uri)
                if (metadata != null) {
                    tracks.add(metadata)
                }
            }
        }

        traverseFiles(directory)
        return tracks
    }

    /**
     * Retrieves metadata for the given audio file Uri using MediaMetadataRetriever.
     *
     * @param contentResolver ContentResolver to open the Uri
     * @param fileUri Uri of the MP3 file
     * @return AudioTrack object with metadata, or null if retrieval fails
     */
    private fun retrieveMetadata(contentResolver: ContentResolver, fileUri: Uri): AudioTrack? {
        val retriever = MediaMetadataRetriever()
        try {
            contentResolver.openFileDescriptor(fileUri, "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)
            }

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown Title"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) ?: "0"
            val duration = durationStr.toLongOrNull() ?: 0L

            return AudioTrack(
                uri = fileUri.toString(),
                title = title,
                artist = artist,
                album = album,
                duration = duration
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            retriever.release()
        }
    }
}
