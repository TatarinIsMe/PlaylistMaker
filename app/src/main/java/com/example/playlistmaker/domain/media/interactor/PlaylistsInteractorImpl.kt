package com.example.playlistmaker.domain.media.interactor

import com.example.playlistmaker.domain.media.repository.PlaylistsRepository
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

class PlaylistsInteractorImpl(
    private val repository: PlaylistsRepository
) : PlaylistsInteractor {

    override suspend fun createPlaylist(name: String, description: String?, coverUri: String?): Long {
        return repository.createPlaylist(name, description, coverUri)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        return repository.addTrackToPlaylist(track, playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return repository.getPlaylists()
    }
}
