package com.example.playlistmaker.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray

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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val playlistTrackRefs = mutableMapOf<Long, MutableList<Long>>()
        db.query("SELECT playlist_id, track_ids_json FROM playlists").use { cursor ->
            val playlistIdColumn = cursor.getColumnIndex("playlist_id")
            val trackIdsColumn = cursor.getColumnIndex("track_ids_json")

            while (cursor.moveToNext()) {
                if (playlistIdColumn == -1 || trackIdsColumn == -1) continue
                val playlistId = cursor.getLong(playlistIdColumn)
                val trackIdsJson = cursor.getString(trackIdsColumn)
                if (trackIdsJson.isNullOrBlank()) continue

                val trackIds = playlistTrackRefs.getOrPut(playlistId) { mutableListOf() }
                val jsonArray = runCatching { JSONArray(trackIdsJson) }.getOrNull() ?: continue
                for (index in 0 until jsonArray.length()) {
                    val trackId = runCatching { jsonArray.getLong(index) }.getOrNull() ?: continue
                    trackIds.add(trackId)
                }
            }
        }

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlists_new` (
                `playlist_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `cover_path` TEXT,
                `tracks_count` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `playlists_new` (`playlist_id`, `name`, `description`, `cover_path`, `tracks_count`)
            SELECT `playlist_id`, `name`, `description`, `cover_path`, `tracks_count` FROM `playlists`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `playlists`")
        db.execSQL("ALTER TABLE `playlists_new` RENAME TO `playlists`")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlist_track_cross_ref` (
                `playlist_id` INTEGER NOT NULL,
                `track_id` INTEGER NOT NULL,
                PRIMARY KEY(`playlist_id`, `track_id`),
                FOREIGN KEY(`playlist_id`) REFERENCES `playlists`(`playlist_id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playlist_track_cross_ref_playlist_id` ON `playlist_track_cross_ref` (`playlist_id`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playlist_track_cross_ref_track_id` ON `playlist_track_cross_ref` (`track_id`)"
        )

        playlistTrackRefs.forEach { (playlistId, trackIds) ->
            trackIds.forEach { trackId ->
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `playlist_track_cross_ref` (`playlist_id`, `track_id`)
                    VALUES (?, ?)
                    """.trimIndent(),
                    arrayOf(playlistId, trackId)
                )
            }
        }
    }
}
