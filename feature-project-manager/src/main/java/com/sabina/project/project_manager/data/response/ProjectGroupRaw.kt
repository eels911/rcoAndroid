package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.project_manager.domain.model.ProjectGroup
import javax.inject.Inject

internal data class ProjectGroupRaw(
    @SerializedName("objects") val objects: List<ProjectObjectRaw> = listOf(),
    @SerializedName("uuid") val uuid: String = "",
    @SerializedName("name") val name: String = "",
) {
    class MapperToProjectGroup @Inject constructor(
        private val mapperToProjectObject: ProjectObjectRaw.MapperToProjectObject
    ) : EssentialMapper<ProjectGroupRaw, ProjectGroup>() {
        override fun transform(raw: ProjectGroupRaw): ProjectGroup {
            return ProjectGroup(
                uuid = raw.uuid,
                name = raw.name,
                objects = raw.objects.essentialMap(mapperToProjectObject)
            )
        }
    }
}