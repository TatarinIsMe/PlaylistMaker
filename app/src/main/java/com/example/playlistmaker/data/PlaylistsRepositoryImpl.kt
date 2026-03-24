package com.example.playlistmaker.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.data.db.toPlaylistTrackEntity
import com.example.playlistmaker.data.db.toDomain
import com.example.playlistmaker.data.db.toEntity
import com.example.playlistmaker.domain.media.repository.PlaylistsRepository
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.google.gson.Gson
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PlaylistsRepositoryImpl(
    private val database: AppDatabase,
    private val context: Context,
    private val gson: Gson
) : PlaylistsRepository {

    override suspend fun createPlaylist(name: String, description: String?, coverUri: String?): Long {
        return withContext(Dispatchers.IO) {
            val savedCoverPath = coverUri?.let { saveCoverToPrivateStorage(Uri.parse(it)) }
            val playlist = PlaylistEntity(
                name = name,
                description = description,
                coverPath = savedCoverPath,
                trackIdsJson = gson.toJson(emptyList<Long>()),
                tracksCount = 0
            )
            database.playlistDao().insertPlaylist(playlist)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        withContext(Dispatchers.IO) {
            database.playlistDao().updatePlaylist(playlist.toEntity(gson))
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                database.withTransaction {
                    val updatedPlaylist = playlist.copy(
                        trackIds = playlist.trackIds + track.trackId,
                        tracksCount = playlist.tracksCount + 1
                    )
                    database.playlistDao().updatePlaylist(updatedPlaylist.toEntity(gson))
                    database.playlistTrackDao().insertTrack(track.toPlaylistTrackEntity())
                }
            }.isSuccess
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return database.playlistDao()
            .getPlaylists()
            .map { playlists -> playlists.map { it.toDomain(gson) } }
    }

    private fun saveCoverToPrivateStorage(imageUri: Uri): String? {
        return runCatching {
            val coversDir = File(context.filesDir, PLAYLIST_COVERS_DIR)
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }

            val extension = resolveFileExtension(imageUri)
            val fileName = "cover_${System.currentTimeMillis()}.$extension"
            val destinationFile = File(coversDir, fileName)

            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null

            destinationFile.absolutePath
        }.getOrNull()
    }

    private fun resolveFileExtension(imageUri: Uri): String {
        val mimeType = context.contentResolver.getType(imageUri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: DEFAULT_IMAGE_EXTENSION
    }

    private companion object {
        const val PLAYLIST_COVERS_DIR = "playlist_covers"
        const val DEFAULT_IMAGE_EXTENSION = "jpg"
    }
}
