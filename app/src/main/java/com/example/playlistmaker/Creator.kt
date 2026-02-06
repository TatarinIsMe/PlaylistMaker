package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.search.network.ItunesApi
import com.example.playlistmaker.data.search.repository.TracksRepositoryImpl
import com.example.playlistmaker.data.search.storage.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.settings.storage.SettingsRepositoryImpl
import com.example.playlistmaker.domain.model.TrackCache
import com.example.playlistmaker.domain.search.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.interactor.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.search.interactor.SearchInteractor
import com.example.playlistmaker.domain.search.interactor.SearchInteractorImpl
import com.example.playlistmaker.domain.settings.interactor.SettingsInteractor
import com.example.playlistmaker.domain.settings.interactor.SettingsInteractorImpl
import com.example.playlistmaker.domain.player.interactor.PlayerInteractor
import com.example.playlistmaker.domain.player.interactor.PlayerInteractorImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Creator(context: Context) {
    private val searchPrefs: SharedPreferences =
        context.getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
    private val settingsPrefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesApi: ItunesApi = retrofit.create(ItunesApi::class.java)

    private val tracksRepository = TracksRepositoryImpl(itunesApi)
    private val historyRepository = SearchHistoryRepositoryImpl(searchPrefs)
    private val settingsRepository = SettingsRepositoryImpl(settingsPrefs)
    val trackCache = TrackCache()

    val playerInteractor: PlayerInteractor by lazy {
        PlayerInteractorImpl(trackCache)
    }



    val searchInteractor: SearchInteractor by lazy { SearchInteractorImpl(tracksRepository) }
    val searchHistoryInteractor: SearchHistoryInteractor by lazy {
        SearchHistoryInteractorImpl(historyRepository)
    }
    val settingsInteractor: SettingsInteractor by lazy {
        SettingsInteractorImpl(settingsRepository)
    }

    companion object {
        private const val BASE_URL = "https://itunes.apple.com/"
    }
}
