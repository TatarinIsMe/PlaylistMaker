package com.example.playlistmaker.data.search.network

import com.example.playlistmaker.data.search.model.TracksSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search?entity=song")
    fun searchTracks(@Query("term") text: String): Call<TracksSearchResponse>
}
