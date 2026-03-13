package com.example.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.player.interactor.PlayerInteractor
import com.example.playlistmaker.domain.search.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.interactor.SearchInteractor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: SearchHistoryInteractor,
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private var searchJob: Job? = null
    private var clickJob: Job? = null
    private var lastQuery = ""
    private var lastFailedQuery = ""
    private var isSearchFieldFocused = false
    private var historyCache: List<Track> = emptyList()
    private var isClickAllowed = true

    private val _state = MutableLiveData(SearchUiState())
    val state: LiveData<SearchUiState> = _state

    private val _navigationEvent = MutableLiveData<Event<Long>>()
    val navigationEvent: LiveData<Event<Long>> = _navigationEvent

    init {
        refreshHistoryCache()
    }

    fun onSearchQueryChanged(query: String) {
        lastQuery = query
        searchJob?.cancel()

        if (query.isBlank()) {
            refreshHistoryCache()
            updateState {
                copy(
                    tracks = emptyList(),
                    isLoading = false,
                    showEmptyPlaceholder = false,
                    showErrorPlaceholder = false
                )
            }
            return
        }

        updateState {
            copy(
                isHistoryVisible = false,
                showEmptyPlaceholder = false,
                showErrorPlaceholder = false
            )
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            executeSearch(query)
        }
    }

    fun onSearchFieldFocusChanged(focused: Boolean) {
        isSearchFieldFocused = focused
        refreshHistoryCache()
    }

    fun onSearchSubmitted() {
        val query = lastQuery.trim()
        if (query.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearch(query)
        }
    }

    fun onRetrySearch() {
        if (lastFailedQuery.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearch(lastFailedQuery)
        }
    }

    fun onTrackSelected(track: Track) {
        if (!isClickAllowed) return

        isClickAllowed = false
        playerInteractor.saveTrack(track)
        historyInteractor.addTrack(track)
        refreshHistoryCache()
        _navigationEvent.value = Event(track.trackId)

        clickJob?.cancel()
        clickJob = viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
            isClickAllowed = true
        }
    }

    fun onClearHistory() {
        historyInteractor.clearHistory()
        refreshHistoryCache()
    }

    private fun refreshHistoryCache() {
        historyCache = historyInteractor.getHistory()
        updateState {
            copy(
                history = historyCache,
                isHistoryVisible = shouldShowHistory()
            )
        }
    }

    private fun shouldShowHistory(): Boolean =
        isSearchFieldFocused && lastQuery.isBlank() && historyCache.isNotEmpty()

    private suspend fun executeSearch(query: String) {
        lastFailedQuery = query
        updateState {
            copy(
                isLoading = true,
                tracks = emptyList(),
                showEmptyPlaceholder = false,
                showErrorPlaceholder = false,
                isHistoryVisible = false
            )
        }

        searchInteractor.searchTracks(query).collect { result ->
            result.fold(
                onSuccess = { tracks ->
                    updateState {
                        copy(
                            isLoading = false,
                            tracks = tracks,
                            showEmptyPlaceholder = tracks.isEmpty(),
                            showErrorPlaceholder = false
                        )
                    }
                },
                onFailure = {
                    updateState {
                        copy(
                            isLoading = false,
                            tracks = emptyList(),
                            showEmptyPlaceholder = false,
                            showErrorPlaceholder = true
                        )
                    }
                }
            )
        }
    }

    private fun updateState(update: SearchUiState.() -> SearchUiState) {
        val current = _state.value ?: SearchUiState()
        _state.value = current.update()
    }
}
