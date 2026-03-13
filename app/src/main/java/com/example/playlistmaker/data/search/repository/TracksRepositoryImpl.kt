package com.example.playlistmaker.data.search.repository

import com.example.playlistmaker.data.search.model.TrackDto
import com.example.playlistmaker.data.search.model.toTrack
import com.example.playlistmaker.data.search.network.ItunesApi
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.search.repository.TracksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

class TracksRepositoryImpl(
    private val itunesApi: ItunesApi
) : TracksRepository {

    override fun searchTracks(query: String): Flow<Result<List<Track>>> = flow {
        try {
            val tracks = itunesApi.searchTracks(query)
                .results
                .map(TrackDto::toTrack)
            emit(Result.success(tracks))
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
