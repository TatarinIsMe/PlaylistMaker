package com.example.playlistmaker.domain.search.repository

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    suspend fun getHistory(): List<Track>
    suspend fun addTrack(track: Track)
    suspend fun clear()
}
