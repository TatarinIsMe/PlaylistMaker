package com.example.playlistmaker.domain.search.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.search.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {
    override fun getHistory(): List<Track> = repository.getHistory()
    override fun addTrack(track: Track) = repository.addTrack(track)
    override fun clearHistory() = repository.clear()
}
