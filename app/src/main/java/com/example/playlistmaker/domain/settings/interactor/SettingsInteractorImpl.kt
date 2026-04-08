package com.example.playlistmaker.domain.settings.interactor

import com.example.playlistmaker.domain.settings.repository.SettingsRepository

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {
    override fun isDarkThemeEnabled(): Boolean = repository.isDarkThemeEnabled()
    override fun switchTheme(enabled: Boolean) = repository.setDarkThemeEnabled(enabled)
}
