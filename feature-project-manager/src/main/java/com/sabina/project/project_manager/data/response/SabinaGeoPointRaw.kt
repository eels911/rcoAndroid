package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.models.SabinaGeoPoint
import javax.inject.Inject

internal data class SabinaGeoPointRaw(
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
) {
    class MapperToSabinaGeoPoint @Inject constructor() : EssentialMapper<SabinaGeoPointRaw, SabinaGeoPoint>() {
        override fun transform(raw: SabinaGeoPointRaw): SabinaGeoPoint {
            return SabinaGeoPoint(raw.latitude, raw.longitude)
        }
    }
}