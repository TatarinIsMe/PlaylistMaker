package com.example.playlistmaker.data.db

import com.example.playlistmaker.domain.model.Track

fun Track.toPlaylistTrackEntity(): PlaylistTrackEntity = PlaylistTrackEntity(
    trackId = trackId,
    artworkUrl100 = artworkUrl100,
    trackName = trackName,
    artistName = artistName,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    trackTime = getFormattedTime(),
    previewUrl = previewUrl
)

fun PlaylistTrackEntity.toTrack(): Track = Track(
    trackId = trackId,
    trackName = trackName,
    artistName = artistName,
    trackTimeMillis = parseTrackTimeToMillis(trackTime),
    artworkUrl100 = artworkUrl100,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    previewUrl = previewUrl,
    isFavorite = false
)

private fun parseTrackTimeToMillis(trackTime: String): Long {
    val parts = trackTime.split(":")
    if (parts.size != 2) return 0L

    val minutes = parts[0].toLongOrNull() ?: return 0L
    val seconds = parts[1].toLongOrNull() ?: return 0L

    return (minutes * 60 + seconds) * 1000
}
