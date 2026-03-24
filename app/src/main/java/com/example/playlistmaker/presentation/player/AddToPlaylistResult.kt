package com.example.playlistmaker.presentation.player

sealed interface AddToPlaylistResult {
    data class Added(val playlistName: String) : AddToPlaylistResult
    data class AlreadyAdded(val playlistName: String) : AddToPlaylistResult
}
