package com.sabina.project.project_manager.presentation.images.gallery

import androidx.constraintlayout.motion.utils.ViewState

internal sealed class GalleryViewState : ViewState() {

    class EmptyList(val hasPermissions: Boolean) : GalleryViewState()
    class FilledList(val images: List<String>) : GalleryViewState()
}