package com.example.playlistmaker

import java.io.Serializable

data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String?,

    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?
) : Serializable {

    // Большая обложка для экрана плеера
    fun getCoverArtwork(): String? =
        artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")

    // Год релиза из releaseDate
    fun getReleaseYear(): String? =
        releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)

    // Формат времени mm:ss (для списка)
    fun getFormattedTime(): String {
        val totalSeconds = trackTimeMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
