package com.example.playlistmaker.domain.player.interactor

import com.example.playlistmaker.domain.model.Track

interface PlayerInteractor {
    fun getTrack(trackId: Long): Track
    fun saveTrack(track: Track)
}