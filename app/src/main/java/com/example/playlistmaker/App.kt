package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    lateinit var creator: Creator
        private set

    var darkTheme: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        creator = Creator(this)
        darkTheme = creator.settingsInteractor.isDarkThemeEnabled()
        applyTheme(darkTheme)
    }

    fun switchTheme(enabled: Boolean) {
        darkTheme = enabled
        creator.settingsInteractor.switchTheme(enabled)
        applyTheme(enabled)
    }

    private fun applyTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
