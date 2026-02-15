package com.example.playlistmaker.di

import com.example.playlistmaker.data.search.repository.TracksRepositoryImpl
import com.example.playlistmaker.data.search.storage.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.settings.storage.SettingsRepositoryImpl
import com.example.playlistmaker.domain.search.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.search.repository.TracksRepository
import com.example.playlistmaker.domain.settings.repository.SettingsRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single<TracksRepository> { TracksRepositoryImpl(get()) }
    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get(named("search_prefs")), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(named("settings_prefs"))) }
}
