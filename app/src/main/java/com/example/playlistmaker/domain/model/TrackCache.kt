package com.example.playlistmaker.domain.model

class TrackCache {
    private val cache = mutableMapOf<Long, Track>()

    fun save(track: Track) {
        cache[track.trackId] = track
    }

    fun get(trackId: Long): Track? = cache[trackId]
}
