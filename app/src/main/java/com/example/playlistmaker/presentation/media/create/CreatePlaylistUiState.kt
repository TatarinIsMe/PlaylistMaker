package com.example.playlistmaker.presentation.media.create

data class CreatePlaylistUiState(
    val name: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val isCreateEnabled: Boolean = false,
    val isSaving: Boolean = false
)
