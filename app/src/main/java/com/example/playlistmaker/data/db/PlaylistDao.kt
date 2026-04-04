package com.example.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRefEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    @Query("UPDATE playlists SET tracks_count = tracks_count + 1 WHERE playlist_id = :playlistId")
    suspend fun incrementTracksCount(playlistId: Long)

    @Transaction
    suspend fun addTrackToPlaylist(
        track: PlaylistTrackEntity,
        crossRef: PlaylistTrackCrossRefEntity
    ): Boolean {
        insertTrack(track)
        val insertedRefId = insertPlaylistTrackCrossRef(crossRef)
        if (insertedRefId == -1L) return false
        incrementTracksCount(crossRef.playlistId)
        return true
    }

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY playlist_id DESC")
    fun getPlaylistsWithTrackRefs(): Flow<List<PlaylistWithTrackRefs>>
}
