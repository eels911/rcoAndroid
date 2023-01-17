package com.sabina.project.project_manager.presentation.map

import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaGeoPoint

internal sealed class MapViewState : ViewState() {

    class DefaultState(
        val userGeolocationMark: SabinaGeoPoint?,
        val mark: SabinaGeoPoint?,
        val hasPermissions: Boolean,
        val isGpsEnabled: Boolean,
    ) : MapViewState()
}