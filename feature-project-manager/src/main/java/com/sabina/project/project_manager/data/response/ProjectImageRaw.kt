package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.NotRequired
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.project_manager.domain.model.ProjectImage
import javax.inject.Inject

internal data class ProjectImageRaw(
    @NotRequired @SerializedName("geo_point") val geoPoint: SabinaGeoPointRaw? = null,
    @SerializedName("uuid") val uuid: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("link") val link: String = "",
    @SerializedName("comment") val comment: String = "",
) {
    class MapperToProjectImage @Inject constructor(
        private val mapperToSabinaGeoPoint: SabinaGeoPointRaw.MapperToSabinaGeoPoint
    ) : EssentialMapper<ProjectImageRaw, ProjectImage>() {
        override fun transform(raw: ProjectImageRaw): ProjectImage {
            return ProjectImage(
                uuid = raw.uuid,
                link = raw.link,
                geoPoint = raw.geoPoint?.essentialMap(mapperToSabinaGeoPoint),
                name = raw.name,
                comment = raw.comment,
            )
        }
    }
}