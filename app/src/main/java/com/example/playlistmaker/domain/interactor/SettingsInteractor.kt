package com.example.playlistmaker.domain.interactor

interface SettingsInteractor {
    fun isDarkThemeEnabled(): Boolean
    fun switchTheme(enabled: Boolean)
}
