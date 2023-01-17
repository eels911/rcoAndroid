package com.sabina.project.project_manager.presentation.project_overview.address

import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.project_manager.domain.model.ProjectAddress

internal sealed class AddressViewState : ViewState() {

    class DefaultState(val address: ProjectAddress, val role: SabinaRoles, val suggestions: List<ProjectAddress>) : AddressViewState()
}