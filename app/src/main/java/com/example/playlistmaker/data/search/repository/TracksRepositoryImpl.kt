package com.example.playlistmaker.data.search.repository

import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.search.model.TrackDto
import com.example.playlistmaker.data.search.model.toTrack
import com.example.playlistmaker.data.search.network.ItunesApi
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.search.repository.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

class TracksRepositoryImpl(
    private val itunesApi: ItunesApi,
    private val database: AppDatabase
) : TracksRepository {

    override fun searchTracks(query: String): Flow<Result<List<Track>>> = flow {
        try {
            val favoriteTrackIds = database.favoriteTrackDao().getFavoriteTrackIds().toSet()
            val tracks = itunesApi.searchTracks(query)
                .results
                .map(TrackDto::toTrack)
                .onEach { track ->
                    track.isFavorite = favoriteTrackIds.contains(track.trackId)
                }
            emit(Result.success(tracks))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
