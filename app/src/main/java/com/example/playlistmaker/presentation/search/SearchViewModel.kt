package com.example.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.search.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.interactor.SearchInteractor

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: SearchHistoryInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var lastQuery = ""
    private var lastFailedQuery = ""
    private var isSearchFieldFocused = false
    private var historyCache: List<Track> = emptyList()

    private val _state = MutableLiveData(SearchUiState())
    val state: LiveData<SearchUiState> = _state

    private val _navigationEvent = MutableLiveData<Event<Track>>()
    val navigationEvent: LiveData<Event<Track>> = _navigationEvent

    init {
        refreshHistoryCache()
    }

    fun onSearchQueryChanged(query: String) {
        lastQuery = query
        searchRunnable?.let { handler.removeCallbacks(it) }
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

        searchRunnable = Runnable { executeSearch(query) }
        handler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY)
    }

    fun onSearchFieldFocusChanged(focused: Boolean) {
        isSearchFieldFocused = focused
        refreshHistoryCache()
    }

    fun onSearchSubmitted() {
        val query = lastQuery.trim()
        if (query.isBlank()) return
        searchRunnable?.let { handler.removeCallbacks(it) }
        executeSearch(query)
    }

    fun onRetrySearch() {
        if (lastFailedQuery.isBlank()) return
        searchRunnable?.let { handler.removeCallbacks(it) }
        executeSearch(lastFailedQuery)
    }

    fun onTrackSelected(track: Track) {
        historyInteractor.addTrack(track)
        refreshHistoryCache()
        _navigationEvent.value = Event(track)
    }

    fun onClearHistory() {
        historyInteractor.clearHistory()
        refreshHistoryCache()
    }

    override fun onCleared() {
        searchRunnable?.let { handler.removeCallbacks(it) }
        super.onCleared()
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

    private fun executeSearch(query: String) {
        searchRunnable = null
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

        searchInteractor.searchTracks(query) { result ->
            result.fold(
                onSuccess = { tracks ->
                    postState {
                        copy(
                            isLoading = false,
                            tracks = tracks,
                            showEmptyPlaceholder = tracks.isEmpty(),
                            showErrorPlaceholder = false
                        )
                    }
                },
                onFailure = {
                    postState {
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

    private fun postState(update: SearchUiState.() -> SearchUiState) {
        val current = _state.value ?: SearchUiState()
        _state.postValue(current.update())
    }
}
