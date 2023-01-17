package com.sabina.project.settings.presentation

import androidx.appcompat.app.AppCompatDelegate
import com.sabina.project.base.external.models.SabinaLanguage

internal sealed class SettingsViewActions {
    class SelectScheme(
        @AppCompatDelegate.NightMode val selectedScheme: Int
    ) : SettingsViewActions()
    object Logout : SettingsViewActions()
    class ShowMessageError(val title: String) : SettingsViewActions()
    class SelectLanguage(
        val language: SabinaLanguage
    ) : SettingsViewActions()
}