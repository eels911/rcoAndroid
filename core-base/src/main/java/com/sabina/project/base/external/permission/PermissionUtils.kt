package com.sabina.project.base.external.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {

    fun getLocationPermissionsArray(): Array<String> {
        return arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun getCameraPermissionsArray(): Array<String> {
        return arrayOf(Manifest.permission.CAMERA)
    }

    fun getGalleryPermissionsArray(): Array<String> {
        return arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun isLocationPermissionsGranted(context: Context): Boolean {
        return context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun isCameraPermissionGranted(context: Context): Boolean {
        return context.hasPermission(Manifest.permission.CAMERA)
    }

    fun isGalleryPermissionGranted(context: Context): Boolean {
        return context.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun requestPermissions(fragment: Fragment, permissions: Array<String>, requestCode: Int) {
        fragment.requestPermissions(
            permissions,
            requestCode
        )
    }

    private fun Context.hasPermission(permissionName: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionName) == PackageManager.PERMISSION_GRANTED
    }
}