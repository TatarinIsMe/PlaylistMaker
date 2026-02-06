package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    lateinit var creator: Creator
        private set

    override fun onCreate() {
        super.onCreate()
        creator = Creator(this)
        applyTheme(creator.settingsInteractor.isDarkThemeEnabled())
    }

    private fun applyTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
