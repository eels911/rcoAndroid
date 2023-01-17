package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.NotRequired
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.project_manager.domain.model.ProjectObject
import com.sabina.project.project_manager.domain.model.ProjectObjectStatus
import javax.inject.Inject

internal data class ProjectObjectRaw(
    @NotRequired @SerializedName("geo_point") val geoPoint: SabinaGeoPointRaw? = null,
    @SerializedName("images") val images: List<ProjectImageRaw> = listOf(),
    @SerializedName("uuid") val uuid: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("type") val type: Int = -1,
    @SerializedName("status") val status: String = "",
    @SerializedName("status_set_by_user_id") val whoSetStatus: String = "",
) {
    class MapperToProjectObject @Inject constructor(
        private val mapperToProjectImage: ProjectImageRaw.MapperToProjectImage,
        private val mapperToSabinaGeoPoint: SabinaGeoPointRaw.MapperToSabinaGeoPoint
    ) : EssentialMapper<ProjectObjectRaw, ProjectObject>() {
        override fun transform(raw: ProjectObjectRaw): ProjectObject {
            return ProjectObject(
                uuid = raw.uuid,
                type = raw.type,
                whoSetStatus = raw.whoSetStatus,
                geoPoint = raw.geoPoint?.essentialMap(mapperToSabinaGeoPoint),
                name = raw.name,
                status = ProjectObjectStatus.byName(raw.status),
                images = raw.images.essentialMap(mapperToProjectImage)
            )
        }
    }
}