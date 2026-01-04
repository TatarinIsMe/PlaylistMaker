package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(
    private val sharedPrefs: SharedPreferences
) {
    companion object {
        private const val KEY_HISTORY = "KEY_SEARCH_HISTORY"
        private const val MAX_SIZE = 10
    }

    private val gson = Gson()

    fun getHistory(): List<Track> {
        val json = sharedPrefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveHistory(list: List<Track>) {
        val json = gson.toJson(list)
        sharedPrefs.edit()
            .putString(KEY_HISTORY, json)
            .apply()
    }

    fun addTrack(track: Track) {
        val mutableHistory = getHistory().toMutableList()
        mutableHistory.removeAll { it.trackId == track.trackId }
        mutableHistory.add(0, track)
        if (mutableHistory.size > MAX_SIZE) {
            mutableHistory.subList(MAX_SIZE, mutableHistory.size).clear()
        }

        saveHistory(mutableHistory)
    }

    fun clear() {
        sharedPrefs.edit()
            .remove(KEY_HISTORY)
            .apply()
    }
}
