package com.example.playlistmaker.domain.search.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.search.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {
    override suspend fun getHistory(): List<Track> = repository.getHistory()
    override suspend fun addTrack(track: Track) = repository.addTrack(track)
    override suspend fun clearHistory() = repository.clear()
}
