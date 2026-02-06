package com.example.playlistmaker.di

import com.example.playlistmaker.domain.model.TrackCache
import com.example.playlistmaker.domain.player.interactor.PlayerInteractor
import com.example.playlistmaker.domain.player.interactor.PlayerInteractorImpl
import com.example.playlistmaker.domain.search.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.interactor.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.search.interactor.SearchInteractor
import com.example.playlistmaker.domain.search.interactor.SearchInteractorImpl
import com.example.playlistmaker.domain.settings.interactor.SettingsInteractor
import com.example.playlistmaker.domain.settings.interactor.SettingsInteractorImpl
import org.koin.dsl.module

val interactorModule = module {
    single { TrackCache() }
    single<SearchInteractor> { SearchInteractorImpl(get()) }
    single<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }
    single<PlayerInteractor> { PlayerInteractorImpl(get()) }
    single<SettingsInteractor> { SettingsInteractorImpl(get()) }
}
