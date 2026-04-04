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
