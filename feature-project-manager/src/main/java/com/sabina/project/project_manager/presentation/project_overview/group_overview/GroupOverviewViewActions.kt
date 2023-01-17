package com.sabina.project.project_manager.presentation.project_overview.group_overview

import com.sabina.project.project_manager.domain.model.ProjectObject

internal sealed class GroupOverviewViewActions {
    object DeleteGroup : GroupOverviewViewActions()

    class SaveInfo(val name: String) : GroupOverviewViewActions()
    object OnBackClick : GroupOverviewViewActions()
    object SaveChangesOnExtraExit : GroupOverviewViewActions()
    object ShowEmptyNameSnack : GroupOverviewViewActions()

    object CreateObject : GroupOverviewViewActions()
    class OpenObject(val item: ProjectObject, val index: Int) : GroupOverviewViewActions()
}