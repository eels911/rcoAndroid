package com.sabina.project.settings.domain

import androidx.appcompat.app.AppCompatDelegate
import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.local_storage.external.ILocalStorageContract
import javax.inject.Inject

internal class Interactor @Inject constructor(
    private val repository: IContract,
    private val localStorageContract: ILocalStorageContract
) {
    fun getColorScheme() = localStorageContract.getColorScheme()
    fun setColorScheme(@AppCompatDelegate.NightMode scheme: Int) = localStorageContract.setColorScheme(scheme)
    fun getLanguage() = localStorageContract.getLanguage()
    fun setLanguage(language: SabinaLanguage) = localStorageContract.setLanguage(language)
}