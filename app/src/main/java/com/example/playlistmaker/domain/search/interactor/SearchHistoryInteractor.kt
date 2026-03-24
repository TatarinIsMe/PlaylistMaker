package com.example.playlistmaker.domain.search.interactor

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryInteractor {
    suspend fun getHistory(): List<Track>
    suspend fun addTrack(track: Track)
    suspend fun clearHistory()
}
