package com.sabina.project.project_manager.domain.model

import android.os.Parcelable
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.NotRequired
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.base.external.models.SabinaGeoPoint
import com.sabina.project.project_manager.data.response.ProjectObjectRaw
import com.sabina.project.project_manager.data.response.SabinaGeoPointRaw
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
internal data class ProjectObject(
    @NotRequired var geoPoint: SabinaGeoPoint? = null,
    var images: List<ProjectImage> = listOf(),
    var name: String = "",
    var type: Int = -1,
    var status: ProjectObjectStatus = ProjectObjectStatus.CREATED,
    var uuid: String = "",
    var whoSetStatus: String = "",
) : Parcelable {

    var isNew = false
    var groupUuid = ""

    class MapperToProjectObjectRaw @Inject constructor(
        private val mapperToProjectImageRaw: ProjectImage.MapperToProjectImageRaw
    ) : EssentialMapper<ProjectObject, ProjectObjectRaw>() {
        override fun transform(raw: ProjectObject): ProjectObjectRaw {
            return ProjectObjectRaw(
                uuid = raw.uuid,
                type = raw.type,
                whoSetStatus = raw.whoSetStatus,
                geoPoint = if (raw.geoPoint == null)
                    null
                else
                    SabinaGeoPointRaw(raw.geoPoint!!.latitude, raw.geoPoint!!.longitude),
                name = raw.name,
                status = raw.status.name,
                images = raw.images.essentialMap(mapperToProjectImageRaw)
            )
        }
    }
}