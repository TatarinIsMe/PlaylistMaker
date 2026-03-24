package com.example.playlistmaker.data

import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.db.toFavoriteTrackEntity
import com.example.playlistmaker.data.db.toTrack
import com.example.playlistmaker.domain.media.repository.FavoritesRepository
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val database: AppDatabase
) : FavoritesRepository {

    override suspend fun addTrack(track: Track) {
        database.favoriteTrackDao().insertTrack(track.toFavoriteTrackEntity())
    }

    override suspend fun removeTrack(track: Track) {
        database.favoriteTrackDao().deleteTrack(track.toFavoriteTrackEntity())
    }

    override fun getFavoriteTracks(): Flow<List<Track>> =
        database.favoriteTrackDao()
            .getFavoriteTracks()
            .map { tracks -> tracks.map { it.toTrack() } }

    override suspend fun getFavoriteTrackIds(): List<Long> =
        database.favoriteTrackDao().getFavoriteTrackIds()
}
