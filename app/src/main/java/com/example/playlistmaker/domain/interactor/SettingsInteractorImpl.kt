package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.SettingsRepository

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {
    override fun isDarkThemeEnabled(): Boolean = repository.isDarkThemeEnabled()
    override fun switchTheme(enabled: Boolean) = repository.setDarkThemeEnabled(enabled)
}
