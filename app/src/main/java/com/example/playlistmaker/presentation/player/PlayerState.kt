package com.example.playlistmaker.presentation.player

data class PlayerState(
    val trackName: String = "",
    val artistName: String = "",
    val durationText: String = "0:00",
    val progressText: String = "0:00",
    val coverUrl: String,

    val albumText: String,
    val yearText: String,
    val genreText: String = "",
    val countryText: String = "",

    val isAlbumVisible: Boolean,
    val isYearVisible: Boolean = false,

    val isPlayEnabled: Boolean = false,
    val isPrepared: Boolean = false,
    val isPlaying: Boolean = false
)