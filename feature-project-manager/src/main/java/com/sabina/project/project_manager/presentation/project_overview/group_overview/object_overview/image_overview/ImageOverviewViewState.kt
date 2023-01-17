package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview.image_overview

import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.project_manager.domain.model.ProjectImage

internal sealed class ImageOverviewViewState : ViewState() {

    class Overview(val image: ProjectImage, val role: SabinaRoles) : ImageOverviewViewState()
    object Creating : ImageOverviewViewState()
}