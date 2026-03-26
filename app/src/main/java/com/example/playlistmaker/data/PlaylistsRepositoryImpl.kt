package com.example.playlistmaker.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.data.db.PlaylistTrackCrossRefEntity
import com.example.playlistmaker.data.db.toPlaylistTrackEntity
import com.example.playlistmaker.data.db.toDomain
import com.example.playlistmaker.data.db.toEntity
import com.example.playlistmaker.domain.media.repository.PlaylistsRepository
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PlaylistsRepositoryImpl(
    private val database: AppDatabase,
    private val context: Context
) : PlaylistsRepository {

    override suspend fun createPlaylist(name: String, description: String?, coverUri: String?): Long {
        val savedCoverPath = coverUri?.let { uri ->
            withContext(Dispatchers.IO) {
                saveCoverToPrivateStorage(Uri.parse(uri))
            }
        }

        val playlist = PlaylistEntity(
            name = name,
            description = description,
            coverPath = savedCoverPath,
            tracksCount = 0
        )

        return withContext(Dispatchers.IO) {
            database.playlistDao().insertPlaylist(playlist)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val playlistEntity = playlist.toEntity()
        withContext(Dispatchers.IO) {
            database.playlistDao().updatePlaylist(playlistEntity)
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                var isAdded = false
                database.withTransaction {
                    database.playlistTrackDao().insertTrack(track.toPlaylistTrackEntity())
                    val insertedRefId = database.playlistDao().insertPlaylistTrackCrossRef(
                        PlaylistTrackCrossRefEntity(
                            playlistId = playlist.playlistId,
                            trackId = track.trackId
                        )
                    )
                    if (insertedRefId != -1L) {
                        database.playlistDao().incrementTracksCount(playlist.playlistId)
                        isAdded = true
                    }
                }
                isAdded
            }.getOrDefault(false)
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return database.playlistDao()
            .getPlaylistsWithTrackRefs()
            .map { playlists -> playlists.map { it.toDomain() } }
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
