package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clear()
}
