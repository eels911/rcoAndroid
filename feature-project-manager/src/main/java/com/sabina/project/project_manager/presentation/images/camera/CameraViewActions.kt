package com.sabina.project.project_manager.presentation.images.camera

internal sealed class CameraViewActions {
    class TakePhoto(val url: String) : CameraViewActions()
    object AcceptImage : CameraViewActions()
    object ClearImage : CameraViewActions()
    class UpdateStatus(val hasPermissions: Boolean) : CameraViewActions()
    class RationalPermissionRequest(val title: String) : CameraViewActions()
}