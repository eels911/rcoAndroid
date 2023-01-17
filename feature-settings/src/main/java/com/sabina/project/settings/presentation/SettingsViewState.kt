package com.sabina.project.settings.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaLanguage

internal sealed class SettingsViewState : ViewState() {

    class DefaultState(
        @AppCompatDelegate.NightMode val selectedScheme: Int,
        val locale: SabinaLanguage
    ) : SettingsViewState()
}