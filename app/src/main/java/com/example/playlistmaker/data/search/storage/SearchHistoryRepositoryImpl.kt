package com.example.playlistmaker.data.search.storage

import android.content.SharedPreferences
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.search.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SearchHistoryRepository {

    companion object {
        private const val KEY_HISTORY = "KEY_SEARCH_HISTORY"
        private const val MAX_SIZE = 10
    }

    private val gson = Gson()

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson<List<Track>>(json, type).orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun addTrack(track: Track) {
        val mutableHistory = getHistory().toMutableList()
        mutableHistory.removeAll { it.trackId == track.trackId }
        mutableHistory.add(0, track)
        if (mutableHistory.size > MAX_SIZE) {
            mutableHistory.subList(MAX_SIZE, mutableHistory.size).clear()
        }
        saveHistory(mutableHistory)
    }

    override fun clear() {
        sharedPreferences.edit()
            .remove(KEY_HISTORY)
            .apply()
    }

    private fun saveHistory(history: List<Track>) {
        sharedPreferences.edit()
            .putString(KEY_HISTORY, gson.toJson(history))
            .apply()
    }
}
