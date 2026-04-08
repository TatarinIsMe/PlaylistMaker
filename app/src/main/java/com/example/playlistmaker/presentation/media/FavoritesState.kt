package com.example.playlistmaker.presentation.media

import com.example.playlistmaker.domain.model.Track

sealed interface FavoritesState {
    data object Empty : FavoritesState
    data class Content(val tracks: List<Track>) : FavoritesState
}
