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

    @Query("DELETE FROM playlists WHERE playlist_id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRefEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    @Query("UPDATE playlists SET tracks_count = tracks_count + 1 WHERE playlist_id = :playlistId")
    suspend fun incrementTracksCount(playlistId: Long)

    @Query("UPDATE playlists SET tracks_count = CASE WHEN tracks_count > 0 THEN tracks_count - 1 ELSE 0 END WHERE playlist_id = :playlistId")
    suspend fun decrementTracksCount(playlistId: Long)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlist_id = :playlistId AND track_id = :trackId")
    suspend fun deletePlaylistTrackCrossRef(playlistId: Long, trackId: Long): Int

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
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): Boolean {
        val deletedRows = deletePlaylistTrackCrossRef(playlistId, trackId)
        if (deletedRows == 0) return false
        decrementTracksCount(playlistId)
        return true
    }

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY playlist_id DESC")
    fun getPlaylistsWithTrackRefs(): Flow<List<PlaylistWithTrackRefs>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE playlist_id = :playlistId LIMIT 1")
    fun getPlaylistWithTrackRefsById(playlistId: Long): Flow<PlaylistWithTrackRefs?>

    @Query("SELECT * FROM playlists WHERE playlist_id = :playlistId LIMIT 1")
    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?>

    @Query("SELECT track_id FROM playlist_track_cross_ref WHERE playlist_id = :playlistId ORDER BY rowid DESC")
    fun getPlaylistTrackIdsByAddedDesc(playlistId: Long): Flow<List<Long>>
}
