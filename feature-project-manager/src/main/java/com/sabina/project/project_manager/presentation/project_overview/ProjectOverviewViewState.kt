package com.sabina.project.project_manager.presentation.project_overview

import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.project_manager.domain.model.Project

internal sealed class ProjectOverviewViewState : ViewState() {

    class Overview(val project: Project, val role: SabinaRoles) : ProjectOverviewViewState()
    object Creating : ProjectOverviewViewState()
}