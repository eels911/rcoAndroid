package com.sabina.project.project_manager.data.response

import com.google.gson.annotations.SerializedName

internal data class AddressSuggestionDataRaw(
    @SerializedName("country") val country: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("house") val building: String? = null,
    @SerializedName("geo_lat") val latitude: Double? = null,
    @SerializedName("geo_lon") val longitude: Double? = null,
    @SerializedName("postal_code") val postalCode: String? = null,
)