package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.extensions.orDefault
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.models.SabinaGeoPoint
import com.sabina.project.project_manager.domain.model.ProjectAddress
import javax.inject.Inject

internal data class AddressSuggestionRaw(
    @SerializedName("value") val value: String = "",
    @SerializedName("data") val data: AddressSuggestionDataRaw,
) {
    class MapperToAddressSuggestion @Inject constructor() : EssentialMapper<AddressSuggestionRaw, ProjectAddress>() {
        override fun transform(raw: AddressSuggestionRaw): ProjectAddress {
            return ProjectAddress(
                country = raw.data.country.orDefault(),
                region = raw.data.region.orDefault(),
                city = raw.data.city.orDefault(),
                street = raw.data.street.orDefault(),
                building = raw.data.building.orDefault(),
                postCode = raw.data.postalCode.orDefault(),
                geoPoint = if (raw.data.latitude == null || raw.data.longitude == null) null else SabinaGeoPoint(raw.data.latitude, raw.data.longitude),
            ).apply {
                this.value = raw.value
            }
        }
    }
}