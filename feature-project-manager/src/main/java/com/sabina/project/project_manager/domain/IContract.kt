package com.sabina.project.project_manager.domain

import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.project_manager.domain.model.AddressSuggestions
import com.sabina.project.project_manager.domain.model.Project

internal interface IContract {
    suspend fun getProjects(): List<Project>

    suspend fun updateProject(project: Project)
    suspend fun deleteProject(uuid: String)

    suspend fun getAddress(query: String, language: SabinaLanguage): AddressSuggestions
    suspend fun createImage(image: ByteArray, uuid: String): String
}