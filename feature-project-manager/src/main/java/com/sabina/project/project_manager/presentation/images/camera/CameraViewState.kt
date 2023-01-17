package com.sabina.project.project_manager.presentation.images.camera

import androidx.constraintlayout.motion.utils.ViewState

internal sealed class CameraViewState : ViewState() {

    class Overview(val url: String) : CameraViewState()
    class Preview(val hasPermissions: Boolean) : CameraViewState()
}