package com.sabina.project.local_storage.internal.data

import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.local_storage.external.ILocalStorageContract
import javax.inject.Inject

internal class LocalStorageRepository @Inject constructor(
    private val userPreferences: UserPreferences
) : ILocalStorageContract {
    override fun getColorScheme(): Int {
        return userPreferences.colorScheme.value
    }

    override fun setColorScheme(scheme: Int) {
        userPreferences.colorScheme.value = scheme
    }

    override fun getLanguage(): SabinaLanguage {
        return SabinaLanguage.enumValueOf(userPreferences.language.value)
    }

    override fun setLanguage(language: SabinaLanguage) {
        userPreferences.language.value = language.code
    }

    override fun getRole(): SabinaRoles {
        return SabinaRoles.enumValueOf(userPreferences.role.value)
    }

    override fun setRole(role: SabinaRoles) {
        userPreferences.role.value = role.code
    }

    override fun getUserId(): String {
        return userPreferences.userId.value
    }

    override fun setUserId(userId: String) {
        userPreferences.userId.value = userId
    }

    override fun clear() {
        userPreferences.clear()
    }
}