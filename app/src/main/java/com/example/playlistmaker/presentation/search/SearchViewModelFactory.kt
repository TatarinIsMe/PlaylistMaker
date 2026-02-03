package com.example.playlistmaker.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.player.interactor.PlayerInteractor
import com.example.playlistmaker.domain.search.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.interactor.SearchInteractor

class SearchViewModelFactory(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: SearchHistoryInteractor,
    private val playerInteractor: PlayerInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(searchInteractor, historyInteractor, playerInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
