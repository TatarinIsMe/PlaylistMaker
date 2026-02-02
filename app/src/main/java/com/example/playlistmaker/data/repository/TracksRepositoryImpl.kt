package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.model.TrackDto
import com.example.playlistmaker.data.model.TracksSearchResponse
import com.example.playlistmaker.data.model.toTrack
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TracksRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(
    private val itunesApi: ItunesApi
) : TracksRepository {

    override fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit) {
        itunesApi.searchTracks(query).enqueue(object : Callback<TracksSearchResponse> {
            override fun onResponse(
                call: Call<TracksSearchResponse>,
                response: Response<TracksSearchResponse>
            ) {
                if (!response.isSuccessful) {
                    callback(Result.failure(RuntimeException("Request failed with code ${response.code()}")))
                    return
                }

                val tracks = response.body()
                    ?.results
                    ?.map(TrackDto::toTrack)
                    ?: emptyList()

                callback(Result.success(tracks))
            }

            override fun onFailure(call: Call<TracksSearchResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }
}
