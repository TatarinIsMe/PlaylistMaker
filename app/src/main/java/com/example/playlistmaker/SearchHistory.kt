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

    fun getHistory(): MutableList<Track> {
        val json = sharedPrefs.getString(KEY_HISTORY, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<Track>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveHistory(list: List<Track>) {
        val json = gson.toJson(list)
        sharedPrefs.edit()
            .putString(KEY_HISTORY, json)
            .apply()
    }

    fun addTrack(track: Track) {
        val history = getHistory()

        // удаляем, если такой уже есть
        history.removeAll { it.trackId == track.trackId }

        // добавляем в начало
        history.add(0, track)

        // обрезаем до 10
        if (history.size > MAX_SIZE) {
            history.subList(MAX_SIZE, history.size).clear()
        }

        saveHistory(history)
    }

    fun clear() {
        sharedPrefs.edit()
            .remove(KEY_HISTORY)
            .apply()
    }
}
