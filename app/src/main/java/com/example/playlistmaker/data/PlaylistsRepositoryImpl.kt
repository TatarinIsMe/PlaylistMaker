package com.example.playlistmaker.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.data.db.PlaylistDao
import com.example.playlistmaker.data.db.PlaylistTrackCrossRefEntity
import com.example.playlistmaker.data.db.PlaylistTrackDao
import com.example.playlistmaker.data.db.toPlaylistTrackEntity
import com.example.playlistmaker.data.db.toDomain
import com.example.playlistmaker.data.db.toEntity
import com.example.playlistmaker.data.db.toTrack
import com.example.playlistmaker.domain.media.repository.PlaylistsRepository
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PlaylistsRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao,
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
            playlistDao.insertPlaylist(playlist)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val playlistEntity = playlist.toEntity()
        withContext(Dispatchers.IO) {
            playlistDao.updatePlaylist(playlistEntity)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val deletedRows = playlistDao.deletePlaylistById(playlistId)
            if (deletedRows > 0) {
                playlistTrackDao.deleteOrphanTracks()
                true
            } else {
                false
            }
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        return withContext(Dispatchers.IO) {
            playlistDao.addTrackToPlaylist(
                track = track.toPlaylistTrackEntity(),
                crossRef = PlaylistTrackCrossRefEntity(
                    playlistId = playlist.playlistId,
                    trackId = track.trackId
                )
            )
        }
    }

    override suspend fun removeTrackFromPlaylist(trackId: Long, playlistId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val removed = playlistDao.removeTrackFromPlaylist(playlistId = playlistId, trackId = trackId)
            if (removed) {
                playlistTrackDao.deleteOrphanTracks()
            }
            removed
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao
            .getPlaylistsWithTrackRefs()
            .map { playlists -> playlists.map { it.toDomain() } }
            .distinctUntilChanged()
    }

    override fun getPlaylistById(playlistId: Long): Flow<Playlist?> {
        return combine(
            playlistDao.getPlaylistById(playlistId),
            playlistDao.getPlaylistTrackIdsByAddedDesc(playlistId)
        ) { playlistEntity, trackIds ->
            playlistEntity?.toDomain(trackIds)
        }.distinctUntilChanged()
    }

    override fun getTracksByIds(trackIds: List<Long>): Flow<List<Track>> {
        if (trackIds.isEmpty()) return flowOf(emptyList())

        return playlistTrackDao.getTracks().map { tracks ->
            val tracksById = tracks.associateBy { track -> track.trackId }
            trackIds.mapNotNull { trackId ->
                tracksById[trackId]?.toTrack()
            }
        }
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
