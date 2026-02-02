package com.example.playlistmaker.domain.repository

interface SettingsRepository {
    fun isDarkThemeEnabled(): Boolean
    fun setDarkThemeEnabled(enabled: Boolean)
}
