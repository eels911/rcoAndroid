package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.mapper.NotRequired
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.project_manager.domain.model.ProjectAddress
import javax.inject.Inject

internal data class ProjectAddressRaw(
    @SerializedName("building") val building: String = "",
    @SerializedName("street") val street: String = "",
    @SerializedName("post_code") val postCode: String = "",
    @SerializedName("region") val region: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("country") val country: String = "",
    @NotRequired @SerializedName("geo_point") val geoPoint: SabinaGeoPointRaw? = null,
) {
    class MapperToProjectAddress @Inject constructor(
        private val mapperToSabinaGeoPoint: SabinaGeoPointRaw.MapperToSabinaGeoPoint
    ) : EssentialMapper<ProjectAddressRaw, ProjectAddress>() {
        override fun transform(raw: ProjectAddressRaw): ProjectAddress {
            return ProjectAddress(
                building = raw.building,
                street = raw.street,
                city = raw.city,
                postCode = raw.postCode,
                region = raw.region,
                country = raw.country,
                geoPoint = raw.geoPoint?.essentialMap(mapperToSabinaGeoPoint),
            )
        }
    }
}