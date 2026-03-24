package com.example.playlistmaker.data.db

import com.example.playlistmaker.domain.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun Playlist.toEntity(gson: Gson): PlaylistEntity = PlaylistEntity(
    playlistId = playlistId,
    name = name,
    description = description,
    coverPath = coverPath,
    trackIdsJson = gson.toJson(trackIds),
    tracksCount = tracksCount
)

fun PlaylistEntity.toDomain(gson: Gson): Playlist = Playlist(
    playlistId = playlistId,
    name = name,
    description = description,
    coverPath = coverPath,
    trackIds = parseTrackIds(trackIdsJson, gson),
    tracksCount = tracksCount
)

private fun parseTrackIds(trackIdsJson: String, gson: Gson): List<Long> {
    val type = object : TypeToken<List<Long>>() {}.type
    return runCatching {
        gson.fromJson<List<Long>>(trackIdsJson, type)
    }.getOrNull().orEmpty()
}
