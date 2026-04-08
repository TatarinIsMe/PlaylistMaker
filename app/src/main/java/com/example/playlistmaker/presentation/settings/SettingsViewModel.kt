package com.example.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.SingleLiveEvent
import com.example.playlistmaker.domain.settings.interactor.SettingsInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _state = MutableLiveData(
        SettingsState(isDarkThemeEnabled = settingsInteractor.isDarkThemeEnabled())
    )
    val state: LiveData<SettingsState> = _state

    private val _effect = SingleLiveEvent<SettingsEffect>()
    val effect: LiveData<SettingsEffect> = _effect

    fun onDarkThemeToggled(enabled: Boolean) {
        settingsInteractor.switchTheme(enabled)
        _state.value = _state.value?.copy(isDarkThemeEnabled = enabled)
            ?: SettingsState(isDarkThemeEnabled = enabled)
    }

    fun onBackClicked() {
        _effect.value = SettingsEffect.Close
    }

    fun onShareClicked(text: String) {
        _effect.value = SettingsEffect.Share(text)
    }

    fun onSupportClicked(email: String, subject: String, body: String) {
        _effect.value = SettingsEffect.SupportEmail(email, subject, body)
    }

    fun onLicenceClicked(url: String) {
        _effect.value = SettingsEffect.OpenUrl(url)
    }
}
