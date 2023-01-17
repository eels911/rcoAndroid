package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.project_manager.domain.model.Project
import javax.inject.Inject

internal data class ProjectRaw(
    @SerializedName("address") val address: ProjectAddressRaw = ProjectAddressRaw(),
    @SerializedName("name") val name: String = "",
    @SerializedName("uuid") val uuid: String = "",
    @SerializedName("create_at") val createAt: Long = 0L,
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("contact") val contact: ProjectContactRaw = ProjectContactRaw(),
    @SerializedName("object_group_list") val objectGroupList: List<ProjectGroupRaw> = listOf(),
) {
    class MapperToProject @Inject constructor(
        private val mapperToProjectAddress: ProjectAddressRaw.MapperToProjectAddress,
        private val mapperToProjectContact: ProjectContactRaw.MapperToProjectContact,
        private val mapperToProjectObjectGroup: ProjectGroupRaw.MapperToProjectGroup,
    ) : EssentialMapper<ProjectRaw, Project>() {
        override fun transform(raw: ProjectRaw): Project {
            return Project(
                uuid = raw.uuid,
                userId = raw.userId,
                createAt = raw.createAt,
                address = raw.address.essentialMap(mapperToProjectAddress),
                name = raw.name,
                contact = raw.contact.essentialMap(mapperToProjectContact),
                objectGroupList = raw.objectGroupList.essentialMap(mapperToProjectObjectGroup)
            )
        }
    }
}