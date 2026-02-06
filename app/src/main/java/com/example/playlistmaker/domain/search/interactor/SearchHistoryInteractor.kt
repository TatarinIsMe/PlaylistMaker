package com.example.playlistmaker.domain.search.interactor

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryInteractor {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}
