package com.example.playlistmaker.data.db

import android.net.Uri
import com.example.playlistmaker.domain.model.Playlist
import java.io.File

fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    playlistId = playlistId,
    name = name,
    description = description,
    coverPath = coverPathFromUri(coverUri),
    tracksCount = tracksCount
)

fun PlaylistWithTrackRefs.toDomain(): Playlist = Playlist(
    playlistId = playlist.playlistId,
    name = playlist.name,
    description = playlist.description,
    coverUri = coverUriFromPath(playlist.coverPath),
    trackIds = trackRefs.map { it.trackId },
    tracksCount = playlist.tracksCount
)

fun PlaylistEntity.toDomain(trackIds: List<Long>): Playlist = Playlist(
    playlistId = playlistId,
    name = name,
    description = description,
    coverUri = coverUriFromPath(coverPath),
    trackIds = trackIds,
    tracksCount = tracksCount
)

private fun coverUriFromPath(coverPath: String?): String? {
    if (coverPath.isNullOrBlank()) return null
    return Uri.fromFile(File(coverPath)).toString()
}

private fun coverPathFromUri(coverUri: String?): String? {
    if (coverUri.isNullOrBlank()) return null
    val uri = runCatching { Uri.parse(coverUri) }.getOrNull() ?: return coverUri
    return when {
        uri.scheme.isNullOrBlank() -> coverUri
        uri.scheme == "file" -> uri.path ?: coverUri
        else -> coverUri
    }
}
