package com.example.playlistmaker.domain.media.interactor

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    suspend fun createPlaylist(name: String, description: String?, coverUri: String?): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long): Boolean
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean
    suspend fun removeTrackFromPlaylist(trackId: Long, playlistId: Long): Boolean
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(playlistId: Long): Flow<Playlist?>
    fun getTracksByIds(trackIds: List<Long>): Flow<List<Track>>
}
