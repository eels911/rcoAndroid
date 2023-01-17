package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview

import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.project_manager.domain.model.ProjectObject

internal sealed class ObjectOverviewViewState : ViewState() {

    class Overview(val obj: ProjectObject, val role: SabinaRoles, val sameTypeObjects: List<ProjectObject>) : ObjectOverviewViewState()
    class Creating(val obj: ProjectObject, val sameTypeObjects: List<ProjectObject>) : ObjectOverviewViewState()
}