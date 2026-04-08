package com.example.playlistmaker.presentation.search

import com.example.playlistmaker.domain.model.Track

data class SearchUiState(
    val isLoading: Boolean = false,
    val showEmptyPlaceholder: Boolean = false,
    val showErrorPlaceholder: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val history: List<Track> = emptyList(),
    val isHistoryVisible: Boolean = false
)
