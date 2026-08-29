package com.example.playlistmaker.presentation.media.playlist

import com.example.playlistmaker.domain.model.Track

data class PlaylistUiState(
    val coverUri: String? = null,
    val name: String = "",
    val description: String? = null,
    val isDescriptionVisible: Boolean = false,
    val totalDurationMinutes: Int = 0,
    val tracksCount: Int = 0,
    val tracks: List<Track> = emptyList()
)
