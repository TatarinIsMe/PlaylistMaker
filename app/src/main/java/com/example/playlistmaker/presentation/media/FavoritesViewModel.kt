package com.example.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.SingleLiveEvent
import com.example.playlistmaker.domain.media.interactor.FavoritesInteractor
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.player.interactor.PlayerInteractor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor,
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesState>(FavoritesState.Empty)
    val state: LiveData<FavoritesState> = _state

    private val _navigationEvent = SingleLiveEvent<Long>()
    val navigationEvent: LiveData<Long> = _navigationEvent

    init {
        observeFavorites()
    }

    fun onTrackClicked(track: Track) {
        playerInteractor.saveTrack(track)
        _navigationEvent.value = track.trackId
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesInteractor.getFavoriteTracks().collect { tracks ->
                _state.postValue(
                    if (tracks.isEmpty()) {
                        FavoritesState.Empty
                    } else {
                        FavoritesState.Content(tracks)
                    }
                )
            }
        }
    }
}
