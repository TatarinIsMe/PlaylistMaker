package com.example.playlistmaker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String?,

    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
) : Parcelable {

    fun getCoverArtwork(): String? =
        artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")

    fun getReleaseYear(): String? =
        releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)

    fun getFormattedTime(): String {
        val totalSeconds = trackTimeMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
