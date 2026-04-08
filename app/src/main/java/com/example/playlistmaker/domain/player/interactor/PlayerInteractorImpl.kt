package com.example.playlistmaker.domain.player.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.model.TrackCache

class PlayerInteractorImpl(
    private val trackCache: TrackCache
) : PlayerInteractor {

    override fun getTrack(trackId: Long): Track =
        trackCache.get(trackId) ?: throw IllegalStateException("Track not found for id $trackId")

    override fun saveTrack(track: Track) {
        trackCache.save(track)
    }
}
