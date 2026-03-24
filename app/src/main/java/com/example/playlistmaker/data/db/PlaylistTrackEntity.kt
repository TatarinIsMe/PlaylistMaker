package com.example.playlistmaker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_tracks")
data class PlaylistTrackEntity(
    @PrimaryKey
    @ColumnInfo(name = "track_id")
    val trackId: Long,
    @ColumnInfo(name = "artwork_url_100")
    val artworkUrl100: String?,
    @ColumnInfo(name = "track_name")
    val trackName: String,
    @ColumnInfo(name = "artist_name")
    val artistName: String,
    @ColumnInfo(name = "collection_name")
    val collectionName: String?,
    @ColumnInfo(name = "release_date")
    val releaseDate: String?,
    @ColumnInfo(name = "primary_genre_name")
    val primaryGenreName: String?,
    @ColumnInfo(name = "country")
    val country: String?,
    @ColumnInfo(name = "track_time")
    val trackTime: String,
    @ColumnInfo(name = "preview_url")
    val previewUrl: String?
)
