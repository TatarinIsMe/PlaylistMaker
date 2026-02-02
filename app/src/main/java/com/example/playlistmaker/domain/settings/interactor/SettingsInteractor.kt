package com.example.playlistmaker.domain.settings.interactor

interface SettingsInteractor {
    fun isDarkThemeEnabled(): Boolean
    fun switchTheme(enabled: Boolean)
}
