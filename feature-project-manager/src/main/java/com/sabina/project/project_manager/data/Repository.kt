package com.sabina.project.project_manager.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.sabina.project.base.external.extensions.asMap
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.project_manager.data.request.AddressRequest
import com.sabina.project.project_manager.data.response.ProjectRaw
import com.sabina.project.project_manager.data.response.SuggestionsRaw
import com.sabina.project.project_manager.domain.IContract
import com.sabina.project.project_manager.domain.model.AddressSuggestions
import com.sabina.project.project_manager.domain.model.Project
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class Repository @Inject constructor(
    private val api: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val mapperToProject: ProjectRaw.MapperToProject,
    private val mapperToProjectRaw: Project.MapperToProjectRaw,
    private val mapperToSuggestions: SuggestionsRaw.MapperToSuggestions,
    private val addressApi: AddressApi,
) : IContract {
    override suspend fun getProjects(): List<Project> {
        val docRef = api.collection("Projects")
        val projects = mutableListOf<Project>()
        val queryDocumentSnapshot = docRef.get().await()
        for (document in queryDocumentSnapshot) {
            val json = Gson().toJson(document.data.toMap())
            val projectRaw: ProjectRaw = Gson().fromJson(json, ProjectRaw::class.java)
            val project = projectRaw.essentialMap(mapperToProject)
            project.uuid = document.id
            projects.add(project)
        }
        return projects.toList()
    }

    override suspend fun updateProject(project: Project) {
        val docRef = api.collection("Projects").document(project.uuid)
        val projectRaw: ProjectRaw = project.essentialMap(mapperToProjectRaw)
        val map = projectRaw.asMap()
        docRef.set(map)
    }

    override suspend fun deleteProject(uuid: String) {
        val docRef = api.collection("Projects").document(uuid)
        docRef.delete()
    }

    override suspend fun getAddress(query: String, language: SabinaLanguage): AddressSuggestions {
        return addressApi.getAddress(AddressRequest(query, language.code)).essentialMap(mapperToSuggestions)
    }

    override suspend fun createImage(image: ByteArray, uuid: String): String {
        val reference = storage.reference.child(uuid)
        reference.putBytes(image).await()
        return storage.reference.child(uuid).downloadUrl.await().toString()
    }

//    override suspend fun deleteImage(uuid: String) {
//        val reference = storage.reference.child(uuid)
//        reference.delete()
//    }
}