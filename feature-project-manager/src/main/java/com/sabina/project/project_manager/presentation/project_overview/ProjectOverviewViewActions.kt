package com.sabina.project.project_manager.presentation.project_overview

import com.sabina.project.project_manager.domain.model.ProjectGroup

internal sealed class ProjectOverviewViewActions {
    object DeleteProject : ProjectOverviewViewActions()

    class SaveInfo(val name: String) : ProjectOverviewViewActions()
    object OnBackClick : ProjectOverviewViewActions()
    object ShowEmptyNameSnack : ProjectOverviewViewActions()
    object SaveChangesOnExtraExit : ProjectOverviewViewActions()

    object OpenContacts : ProjectOverviewViewActions()
    object OpenAddress : ProjectOverviewViewActions()

    object CreateGroup : ProjectOverviewViewActions()
    class OpenGroup(val item: ProjectGroup, val index: Int) : ProjectOverviewViewActions()
}