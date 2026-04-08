package com.example.playlistmaker.domain.model

data class Playlist(
    val playlistId: Long = 0,
    val name: String,
    val description: String?,
    val coverUri: String?,
    val trackIds: List<Long> = emptyList(),
    val tracksCount: Int = 0
)
