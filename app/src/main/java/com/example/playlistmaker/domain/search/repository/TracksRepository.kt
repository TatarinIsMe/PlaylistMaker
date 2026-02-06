package com.example.playlistmaker.domain.search.repository

import com.example.playlistmaker.domain.model.Track

interface TracksRepository {
    fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit)
}
