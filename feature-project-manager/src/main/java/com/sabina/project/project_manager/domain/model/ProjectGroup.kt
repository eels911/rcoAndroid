package com.sabina.project.project_manager.domain.model

import android.os.Parcelable
import com.sabina.project.base.external.extensions.orDefault
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.project_manager.data.response.ProjectGroupRaw
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
internal data class ProjectGroup(
    var objects: List<ProjectObject> = listOf(),
    var name: String = "",
    var uuid: String = "",
) : Parcelable {

    var isNew = false
    class MapperToProjectGroupRaw @Inject constructor(
        private val mapperToProjectObjectRaw: ProjectObject.MapperToProjectObjectRaw
    ) : EssentialMapper<ProjectGroup, ProjectGroupRaw>() {
        override fun transform(raw: ProjectGroup): ProjectGroupRaw {
            return ProjectGroupRaw(
                uuid = raw.uuid,
                name = raw.name.orDefault(),
                objects = raw.objects.essentialMap(mapperToProjectObjectRaw)
            )
        }
    }
}