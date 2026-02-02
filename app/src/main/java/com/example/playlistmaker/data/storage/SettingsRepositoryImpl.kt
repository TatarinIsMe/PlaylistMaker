package com.example.playlistmaker.data.storage

import android.content.SharedPreferences
import com.example.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    override fun isDarkThemeEnabled(): Boolean =
        sharedPreferences.getBoolean(KEY_DARK_THEME, false)

    override fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_DARK_THEME, enabled)
            .apply()
    }
}
