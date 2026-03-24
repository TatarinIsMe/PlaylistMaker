package com.example.playlistmaker.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlists` (
                `playlist_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `cover_path` TEXT,
                `track_ids_json` TEXT NOT NULL,
                `tracks_count` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlist_tracks` (
                `track_id` INTEGER NOT NULL,
                `artwork_url_100` TEXT,
                `track_name` TEXT NOT NULL,
                `artist_name` TEXT NOT NULL,
                `collection_name` TEXT,
                `release_date` TEXT,
                `primary_genre_name` TEXT,
                `country` TEXT,
                `track_time` TEXT NOT NULL,
                `preview_url` TEXT,
                PRIMARY KEY(`track_id`)
            )
            """.trimIndent()
        )
    }
}
