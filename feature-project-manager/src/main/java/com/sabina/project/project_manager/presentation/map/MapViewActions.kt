package com.sabina.project.project_manager.presentation.map

import android.location.Location
import com.sabina.project.base.external.models.SabinaGeoPoint

internal sealed class MapViewActions {
    class PutMark(val mark: SabinaGeoPoint) : MapViewActions()
    class PutUserGeolocationMark(val userGeolocationMark: SabinaGeoPoint) : MapViewActions()
    class UpdateStatus(
        val hasPermissions: Boolean,
        val isGpsEnabled: Boolean
    ) : MapViewActions()

    object SaveMark : MapViewActions()
    object RationalPermissionRequest : MapViewActions()
    object OnBackClick : MapViewActions()
    class LoadLocation(
        val onSuccess: (location: Location) -> Unit
    ) : MapViewActions()
}