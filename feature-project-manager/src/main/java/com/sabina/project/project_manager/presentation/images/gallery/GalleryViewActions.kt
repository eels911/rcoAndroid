package com.sabina.project.project_manager.presentation.images.gallery

internal sealed class GalleryViewActions {
    class SelectImage(val image: String) : GalleryViewActions()
    class UpdateState(val images: List<String>) : GalleryViewActions()
    class UpdateStatus(val hasPermissions: Boolean) : GalleryViewActions()
    class RationalPermissionRequest(val title: String) : GalleryViewActions()
}