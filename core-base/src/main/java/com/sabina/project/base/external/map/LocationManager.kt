package com.sabina.project.base.external.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.sabina.project.base.external.permission.PermissionUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @SuppressLint("MissingPermission")
    fun loadLocation(block: ((Location?) -> Unit)? = null) {
        var locationManager: LocationManager? = null

        if (PermissionUtils.isLocationPermissionsGranted(context)) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (MapUtils.isGpsEnabled(context)) {
                defineLocation(block, locationManager)
            } else {
                block?.invoke(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
            }
        } else {
            block?.invoke(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun defineLocation(block: ((Location?) -> Unit)?, locationManager: LocationManager) {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH

        locationManager.requestSingleUpdate(criteria, object : LocationListener {
            override fun onLocationChanged(location: Location) {
                block?.invoke(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {
                block?.invoke(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
            }
        }, null)
    }
}