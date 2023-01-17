package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview

import com.sabina.project.project_manager.domain.model.ProjectImage
import com.sabina.project.project_manager.domain.model.ProjectObjectStatus

internal sealed class ObjectOverviewViewActions {
    object DeleteObject : ObjectOverviewViewActions()

    class SaveInfo(
        val name: String,
        val type: Int
    ) : ObjectOverviewViewActions()

    class SetStatus(val status: ProjectObjectStatus) : ObjectOverviewViewActions()
    class SelectType(val type: Int) : ObjectOverviewViewActions()
    object OnBackClick : ObjectOverviewViewActions()
    object SaveChangesOnExtraExit : ObjectOverviewViewActions()
    object OpenMap : ObjectOverviewViewActions()
    object ShowEmptyNameSnack : ObjectOverviewViewActions()
    object ShowSelectTypeSnack : ObjectOverviewViewActions()

    object CreateImage : ObjectOverviewViewActions()
    class OpenImage(val item: ProjectImage, val index: Int) : ObjectOverviewViewActions()
}