package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.project_manager.domain.model.ProjectContact
import javax.inject.Inject

internal data class ProjectContactRaw(
    @SerializedName("email") val email: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("name") val name: String = "",
) {
    class MapperToProjectContact @Inject constructor() : EssentialMapper<ProjectContactRaw, ProjectContact>() {
        override fun transform(raw: ProjectContactRaw): ProjectContact {
            return ProjectContact(
                email = raw.email,
                phone = raw.phone,
                name = raw.name,
            )
        }
    }
}