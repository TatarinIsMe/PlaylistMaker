package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TracksRepository

class SearchInteractorImpl(
    private val tracksRepository: TracksRepository
) : SearchInteractor {
    override fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit) {
        tracksRepository.searchTracks(query, callback)
    }
}
