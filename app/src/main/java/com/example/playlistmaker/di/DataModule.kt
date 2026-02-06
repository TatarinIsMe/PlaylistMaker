package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.data.search.network.ItunesApi
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ITUNES_BASE_URL = "https://itunes.apple.com/"

val dataModule = module {
    single {
        Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(ItunesApi::class.java)
    }

    single(named("search_prefs")) {
        androidContext().getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
    }

    single(named("settings_prefs")) {
        androidContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    factory { Gson() }
}
