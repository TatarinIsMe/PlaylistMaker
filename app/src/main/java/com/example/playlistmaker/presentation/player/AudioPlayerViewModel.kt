package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.media.interactor.FavoritesInteractor
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.player.interactor.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    trackId: Long,
    private val playerInteractor: PlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val track: Track = playerInteractor.getTrack(trackId)

    companion object {
        private const val UPDATE_PROGRESS_DELAY = 300L
    }

    private val _state = MutableLiveData(createInitialState(track))
    val state: LiveData<PlayerState> = _state

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var isPrepared = false

    init {
        preparePlayer()
        loadFavoriteState()
    }

    fun onPlayPauseClicked() {
        if (!isPrepared) return
        val player = mediaPlayer ?: return

        val current = _state.value ?: createInitialState(track)

        if (current.isPlaying) {
            player.pause()
            stopProgressUpdates()
            updateState {
                copy(
                    isPlaying = false,
                    progressText = formatTime(player.currentPosition.toLong())
                )
            }
        } else {
            if (player.currentPosition >= player.duration) {
                player.seekTo(0)
                updateState { copy(progressText = formatTime(0L)) }
            }

            player.start()
            updateState { copy(isPlaying = true) }
            startProgressUpdates()
        }
    }

    fun onPausePlayback() {
        val player = mediaPlayer ?: return
        if (_state.value?.isPlaying != true) return

        player.pause()
        stopProgressUpdates()
        updateState {
            copy(
                isPlaying = false,
                progressText = formatTime(player.currentPosition.toLong())
            )
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            val isFavoriteNow = _state.value?.isFavorite ?: track.isFavorite

            if (isFavoriteNow) {
                favoritesInteractor.removeTrack(track)
            } else {
                favoritesInteractor.addTrack(track)
            }

            track.isFavorite = !isFavoriteNow
            updateState { copy(isFavorite = track.isFavorite) }
        }
    }

    override fun onCleared() {
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }

    private fun preparePlayer() {
        val url = track.previewUrl
        if (url.isNullOrBlank()) {
            updateState { copy(isPlayEnabled = false) }
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)

            setOnPreparedListener {
                isPrepared = true
                updateState { copy(isPrepared = true) }
            }

            setOnCompletionListener {
                stopProgressUpdates()
                updateState {
                    copy(
                        isPlaying = false,
                        progressText = formatTime(0L)
                    )
                }
            }

            prepareAsync()
        }
    }

    private fun loadFavoriteState() {
        viewModelScope.launch {
            val isFavorite = favoritesInteractor.isFavorite(track.trackId)
            track.isFavorite = isFavorite
            updateState { copy(isFavorite = isFavorite) }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()

        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(UPDATE_PROGRESS_DELAY)
                val player = mediaPlayer ?: break
                updateState { copy(progressText = formatTime(player.currentPosition.toLong())) }
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateState(update: PlayerState.() -> PlayerState) {
        val current = _state.value ?: createInitialState(track)
        _state.value = current.update()
    }

    private fun createInitialState(track: Track): PlayerState {
        val album = track.collectionName?.takeIf { it.isNotBlank() } ?: ""
        val year = track.getReleaseYear()?.takeIf { it.isNotBlank() } ?: ""
        val cover = track.getCoverArtwork() ?: ""

        return PlayerState(
            trackName = track.trackName.orEmpty(),
            artistName = track.artistName.orEmpty(),
            durationText = track.getFormattedTime(),
            progressText = formatTime(0L),
            coverUrl = cover,

            albumText = album,
            yearText = year,
            genreText = track.primaryGenreName.orEmpty(),
            countryText = track.country.orEmpty(),

            isAlbumVisible = album.isNotEmpty(),
            isYearVisible = year.isNotEmpty(),

            isPlayEnabled = !track.previewUrl.isNullOrBlank(),
            isPrepared = false,
            isPlaying = false,
            isFavorite = track.isFavorite
        )
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
