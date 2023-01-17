package com.sabina.project.project_manager.presentation.project_overview.group_overview

import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.project_manager.domain.model.ProjectGroup
import com.sabina.project.project_manager.domain.model.ProjectObject

internal sealed class GroupOverviewViewState : ViewState() {

    class Overview(val group: ProjectGroup, val role: SabinaRoles, val objectList: List<ProjectObject>) : GroupOverviewViewState()
    object Creating : GroupOverviewViewState()
}