package com.sabina.project.presentation.domain

import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.local_storage.external.ILocalStorageContract
import javax.inject.Inject

class Interactor @Inject constructor(
    private val repository: IContract,
    private val iLocalStorageContract: ILocalStorageContract
) {
    fun clearSharedPref() = iLocalStorageContract.clear()
    fun setUserId(userId: String) = iLocalStorageContract.setUserId(userId)
    fun getUserId(): String = iLocalStorageContract.getUserId()
    fun setRole(role: SabinaRoles) = iLocalStorageContract.setRole(role)
    suspend fun getRole(): SabinaRoles = repository.getRole(iLocalStorageContract.getUserId())
    fun signOut() {
        repository.signOut()
    }
    fun setLanguage(language: SabinaLanguage) {
        iLocalStorageContract.setLanguage(language)
    }
    fun getLanguage() = iLocalStorageContract.getLanguage()
}