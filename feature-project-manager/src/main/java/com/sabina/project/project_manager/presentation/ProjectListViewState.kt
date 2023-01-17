package com.sabina.project.project_manager.presentation

import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.project_manager.domain.model.Project

internal sealed class ProjectListViewState : ViewState() {

    class EmptyList(
        val role: SabinaRoles
    ) : ProjectListViewState()
    class FilledList(
        val role: SabinaRoles,
        val projects: List<Project>,
        val isSearchEnabled: Boolean,
    ) : ProjectListViewState()
}