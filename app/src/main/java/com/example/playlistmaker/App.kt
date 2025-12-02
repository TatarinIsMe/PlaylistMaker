package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    private lateinit var prefs: SharedPreferences

    var darkTheme: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        darkTheme = prefs.getBoolean(KEY_DARK_THEME, false)
        applyTheme(darkTheme)
    }

    fun switchTheme(enabled: Boolean) {
        darkTheme = enabled

        prefs.edit()
            .putBoolean(KEY_DARK_THEME, enabled).apply()

        applyTheme(enabled)
    }

    private fun applyTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    companion object {
        private const val PREFS = "settings"
        private const val KEY_DARK_THEME = "dark_theme"
    }
}
