package com.example.playlistmaker.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistWithTrackRefs(
    @Embedded
    val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "playlist_id",
        entityColumn = "playlist_id"
    )
    val trackRefs: List<PlaylistTrackCrossRefEntity>
)
