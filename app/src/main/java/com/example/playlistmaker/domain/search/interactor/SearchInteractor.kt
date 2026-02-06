package com.example.playlistmaker.domain.search.interactor

import com.example.playlistmaker.domain.model.Track

interface SearchInteractor {
    fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit)
}
