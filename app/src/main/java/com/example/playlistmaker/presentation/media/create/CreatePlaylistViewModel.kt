package com.example.playlistmaker.presentation.media.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.SingleLiveEvent
import com.example.playlistmaker.domain.media.interactor.PlaylistsInteractor
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData(CreatePlaylistUiState())
    val uiState: LiveData<CreatePlaylistUiState> = _uiState

    private val _playlistCreatedEvent = SingleLiveEvent<String>()
    val playlistCreatedEvent: LiveData<String> = _playlistCreatedEvent

    fun onNameChanged(name: String) {
        updateState { state ->
            state.copy(name = name)
        }
    }

    fun onDescriptionChanged(description: String) {
        updateState { state ->
            state.copy(description = description)
        }
    }

    fun onCoverSelected(coverUri: String) {
        updateState { state ->
            state.copy(coverUri = coverUri)
        }
    }

    fun hasUnsavedData(): Boolean {
        val state = _uiState.value ?: return false
        return state.name.isNotBlank() || state.description.isNotBlank() || !state.coverUri.isNullOrBlank()
    }

    fun onCreateClicked() {
        val state = _uiState.value ?: return
        val name = state.name.trim()
        if (name.isBlank() || state.isSaving) return

        viewModelScope.launch {
            updateState { current -> current.copy(isSaving = true) }

            runCatching {
                playlistsInteractor.createPlaylist(
                    name = name,
                    description = state.description.trim().takeIf { it.isNotEmpty() },
                    coverUri = state.coverUri
                )
            }.onSuccess {
                _playlistCreatedEvent.value = name
            }.onFailure {
                updateState { current -> current.copy(isSaving = false) }
            }
        }
    }

    private fun updateState(transform: (CreatePlaylistUiState) -> CreatePlaylistUiState) {
        val currentState = _uiState.value ?: CreatePlaylistUiState()
        val newState = transform(currentState)
        _uiState.value = newState.copy(
            isCreateEnabled = newState.name.isNotBlank() && !newState.isSaving
        )
    }
}
