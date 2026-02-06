package com.example.playlistmaker.presentation.settings

sealed interface SettingsEffect {
    object Close : SettingsEffect

    data class Share(val text: String) : SettingsEffect

    data class SupportEmail(
        val email: String,
        val subject: String,
        val body: String
    ) : SettingsEffect

    data class OpenUrl(val url: String) : SettingsEffect
}
