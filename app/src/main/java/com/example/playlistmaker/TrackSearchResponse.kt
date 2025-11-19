package com.example.playlistmaker

data class TracksSearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)

data class TrackDto(
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?
)