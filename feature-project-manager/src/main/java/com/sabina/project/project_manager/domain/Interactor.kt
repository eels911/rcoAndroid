package com.sabina.project.project_manager.domain

import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.local_storage.external.ILocalStorageContract
import com.sabina.project.project_manager.domain.model.Project
import javax.inject.Inject

internal class Interactor @Inject constructor(
    private val repository: IContract,
    private val localStorageContract: ILocalStorageContract,
) {
    suspend fun getProjects() = repository.getProjects()
    suspend fun getAddress(query: String) = repository.getAddress(query, localStorageContract.getLanguage())

    suspend fun updateProject(project: Project) = repository.updateProject(project)
    suspend fun deleteProject(uuid: String) = repository.deleteProject(uuid)

    suspend fun createImage(image: ByteArray, uuid: String): String = repository.createImage(image, uuid)
    fun getUserId(): String = localStorageContract.getUserId()
    fun getRole(): SabinaRoles = localStorageContract.getRole()
}