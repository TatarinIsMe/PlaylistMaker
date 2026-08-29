package com.example.playlistmaker.presentation.media.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.SingleLiveEvent
import com.example.playlistmaker.domain.media.interactor.PlaylistsInteractor
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.player.interactor.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistViewModel(
    playlistId: Long,
    private val playlistsInteractor: PlaylistsInteractor,
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val formatter = SimpleDateFormat("mm", Locale.getDefault())

    private val _uiState = MutableLiveData(PlaylistUiState())
    val uiState: LiveData<PlaylistUiState> = _uiState

    private val _navigationEvent = SingleLiveEvent<Long>()
    val navigationEvent: LiveData<Long> = _navigationEvent

    private val _playlistDeletedEvent = SingleLiveEvent<Unit>()
    val playlistDeletedEvent: LiveData<Unit> = _playlistDeletedEvent

    private var currentPlaylist: Playlist? = null
    private var tracksJob: Job? = null

    init {
        observePlaylist(playlistId)
    }

    fun onTrackClicked(track: Track) {
        playerInteractor.saveTrack(track)
        _navigationEvent.value = track.trackId
    }

    fun onDeleteTrackConfirmed(track: Track) {
        val playlistId = currentPlaylist?.playlistId ?: return
        viewModelScope.launch {
            playlistsInteractor.removeTrackFromPlaylist(track.trackId, playlistId)
        }
    }

    fun onDeletePlaylistConfirmed() {
        val playlistId = currentPlaylist?.playlistId ?: return
        viewModelScope.launch {
            val isDeleted = playlistsInteractor.deletePlaylist(playlistId)
            if (isDeleted) {
                _playlistDeletedEvent.value = Unit
            }
        }
    }

    private fun observePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistsInteractor.getPlaylistById(playlistId)
                .collect { playlist ->
                    tracksJob?.cancel()
                    currentPlaylist = playlist

                    if (playlist == null) {
                        _uiState.value = PlaylistUiState()
                        return@collect
                    }

                    tracksJob = viewModelScope.launch {
                        playlistsInteractor.getTracksByIds(playlist.trackIds).collect { tracks ->
                            val durationSum = tracks.sumOf { track -> track.trackTimeMillis }
                            val totalDurationMinutes = formatter.format(durationSum).toIntOrNull() ?: 0

                            _uiState.value = PlaylistUiState(
                                coverUri = playlist.coverUri,
                                name = playlist.name,
                                description = playlist.description,
                                isDescriptionVisible = !playlist.description.isNullOrBlank(),
                                totalDurationMinutes = totalDurationMinutes,
                                tracksCount = playlist.tracksCount,
                                tracks = tracks
                            )
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        tracksJob?.cancel()
        tracksJob = null
        super.onCleared()
    }
}
