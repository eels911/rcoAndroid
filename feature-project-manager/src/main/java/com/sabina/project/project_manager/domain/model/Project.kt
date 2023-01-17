package com.sabina.project.project_manager.domain.model

import android.os.Parcelable
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.project_manager.data.response.ProjectRaw
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
internal data class Project(
    var address: ProjectAddress = ProjectAddress(),
    var name: String = "",
    var objectGroupList: List<ProjectGroup> = listOf(),
    var contact: ProjectContact = ProjectContact(),
    var createAt: Long,
    var uuid: String,
    var userId: String
) : Parcelable {

    var isNew: Boolean = false

    class MapperToProjectRaw @Inject constructor(
        private val mapperToProjectAddressRaw: ProjectAddress.MapperToProjectAddressRaw,
        private val mapperToProjectContactRaw: ProjectContact.MapperToProjectContactRaw,
        private val mapperToProjectObjectGroupRaw: ProjectGroup.MapperToProjectGroupRaw,
    ) : EssentialMapper<Project, ProjectRaw>() {
        override fun transform(raw: Project): ProjectRaw {
            return ProjectRaw(
                userId = raw.userId,
                uuid = raw.uuid,
                createAt = raw.createAt,
                address = raw.address.essentialMap(mapperToProjectAddressRaw),
                name = raw.name,
                contact = raw.contact.essentialMap(mapperToProjectContactRaw),
                objectGroupList = raw.objectGroupList.essentialMap(mapperToProjectObjectGroupRaw)
            )
        }
    }
}