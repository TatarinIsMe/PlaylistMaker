package com.example.playlistmaker.di

import com.example.playlistmaker.presentation.player.AudioPlayerViewModel
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import com.example.playlistmaker.presentation.media.create.CreatePlaylistViewModel
import com.example.playlistmaker.presentation.media.FavoritesViewModel
import com.example.playlistmaker.presentation.media.MediaViewModel
import com.example.playlistmaker.presentation.media.PlaylistsViewModel
import com.example.playlistmaker.presentation.media.playlist.PlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SearchViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { (trackId: Long) -> AudioPlayerViewModel(trackId, get(), get(), get()) }
    viewModel { MediaViewModel() }
    viewModel { PlaylistsViewModel(get()) }
    viewModel { (editablePlaylistId: Long?) -> CreatePlaylistViewModel(get(), editablePlaylistId) }
    viewModel { (playlistId: Long) -> PlaylistViewModel(playlistId, get(), get()) }
    viewModel { FavoritesViewModel(get(), get()) }
}
