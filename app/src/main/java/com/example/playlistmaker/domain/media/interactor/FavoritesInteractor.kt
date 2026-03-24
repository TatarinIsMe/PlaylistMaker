package com.example.playlistmaker.domain.media.interactor

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesInteractor {
    suspend fun addTrack(track: Track)
    suspend fun removeTrack(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Long): Boolean
    suspend fun getFavoriteTrackIds(): Set<Long>
}
