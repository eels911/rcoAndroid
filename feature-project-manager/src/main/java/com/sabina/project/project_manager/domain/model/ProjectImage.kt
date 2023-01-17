package com.sabina.project.project_manager.domain.model

import android.os.Parcelable
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.NotRequired
import com.sabina.project.base.external.models.SabinaGeoPoint
import com.sabina.project.project_manager.data.response.ProjectImageRaw
import com.sabina.project.project_manager.data.response.SabinaGeoPointRaw
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
internal data class ProjectImage(
    @NotRequired var geoPoint: SabinaGeoPoint? = null,
    var name: String = "",
    var comment: String = "",
    var uuid: String = "",
    var link: String = "",
) : Parcelable {

    var isNew = false
    var hasImage = true

    class MapperToProjectImageRaw @Inject constructor() : EssentialMapper<ProjectImage, ProjectImageRaw>() {
        override fun transform(raw: ProjectImage): ProjectImageRaw {
            return ProjectImageRaw(
                uuid = raw.uuid,
                link = raw.link,
                geoPoint = if (raw.geoPoint == null)
                    null
                else
                    SabinaGeoPointRaw(raw.geoPoint!!.latitude, raw.geoPoint!!.longitude),
                name = raw.name,
                comment = raw.comment,
            )
        }
    }
}