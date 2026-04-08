package com.example.playlistmaker.domain.media.interactor

import com.example.playlistmaker.domain.media.repository.FavoritesRepository
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

class FavoritesInteractorImpl(
    private val repository: FavoritesRepository
) : FavoritesInteractor {

    override suspend fun addTrack(track: Track) {
        repository.addTrack(track)
    }

    override suspend fun removeTrack(track: Track) {
        repository.removeTrack(track)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> = repository.getFavoriteTracks()

    override suspend fun isFavorite(trackId: Long): Boolean =
        repository.getFavoriteTrackIds().contains(trackId)

    override suspend fun getFavoriteTrackIds(): Set<Long> =
        repository.getFavoriteTrackIds().toSet()
}
