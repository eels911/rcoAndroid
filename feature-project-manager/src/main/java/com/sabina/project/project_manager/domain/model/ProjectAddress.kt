package com.sabina.project.project_manager.domain.model

import android.os.Parcelable
import com.sabina.project.base.external.extensions.orDefault
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.NotRequired
import com.sabina.project.base.external.models.SabinaGeoPoint
import com.sabina.project.project_manager.data.response.ProjectAddressRaw
import com.sabina.project.project_manager.data.response.SabinaGeoPointRaw
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
internal data class ProjectAddress(
    var building: String = "",
    var country: String = "",
    var postCode: String = "",
    var region: String = "",
    var city: String = "",
    var street: String = "",
    @NotRequired var geoPoint: SabinaGeoPoint? = null,
) : Parcelable {

    var value = ""

    class MapperToProjectAddressRaw @Inject constructor() : EssentialMapper<ProjectAddress, ProjectAddressRaw>() {
        override fun transform(raw: ProjectAddress): ProjectAddressRaw {
            return ProjectAddressRaw(
                building = raw.building,
                street = raw.street,
                city = raw.city,
                postCode = raw.postCode.orDefault(),
                region = raw.region.orDefault(),
                country = raw.country.orDefault(),
                geoPoint = if (raw.geoPoint == null)
                    null
                else
                    SabinaGeoPointRaw(raw.geoPoint!!.latitude, raw.geoPoint!!.longitude)
            )
        }
    }
}