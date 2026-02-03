package com.example.playlistmaker.domain.search.repository

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clear()
}
