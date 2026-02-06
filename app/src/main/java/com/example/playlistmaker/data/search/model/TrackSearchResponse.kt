package com.example.playlistmaker.data.search.model

import com.example.playlistmaker.domain.model.Track

data class TracksSearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)

data class TrackDto(
    val trackId: Long?,
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,

    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
)

// Mapper from network DTO to domain model.
fun TrackDto.toTrack() = Track(
    trackId = trackId ?: 0L,
    trackName = trackName.orEmpty(),
    artistName = artistName.orEmpty(),
    trackTimeMillis = trackTimeMillis ?: 0L,
    artworkUrl100 = artworkUrl100,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    previewUrl = previewUrl
)
