package com.sabina.project.base.external.models

import android.os.Parcelable
import com.yandex.mapkit.geometry.Point
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SabinaGeoPoint(
    val latitude: Double,
    val longitude: Double
) : Parcelable

fun SabinaGeoPoint.asMapKitPoint(): Point {
    return Point(this.latitude, this.longitude)
}

fun Point.asSabinaGeoPoint(): SabinaGeoPoint {
    return SabinaGeoPoint(this.latitude, this.longitude)
}