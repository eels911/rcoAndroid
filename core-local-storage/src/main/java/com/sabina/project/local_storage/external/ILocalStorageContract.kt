package com.sabina.project.local_storage.external

import androidx.appcompat.app.AppCompatDelegate
import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.base.external.models.SabinaRoles

interface ILocalStorageContract {
    fun getColorScheme(): Int
    fun setColorScheme(@AppCompatDelegate.NightMode scheme: Int)

    fun getLanguage(): SabinaLanguage
    fun setLanguage(language: SabinaLanguage)

    fun getRole(): SabinaRoles
    fun setRole(role: SabinaRoles)

    fun getUserId(): String
    fun setUserId(userId: String)

    fun clear()
}