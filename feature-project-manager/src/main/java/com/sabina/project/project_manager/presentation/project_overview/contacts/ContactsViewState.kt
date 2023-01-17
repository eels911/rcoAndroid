package com.sabina.project.project_manager.presentation.project_overview.contacts

import androidx.constraintlayout.motion.utils.ViewState
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.project_manager.domain.model.ProjectContact

internal sealed class ContactsViewState : ViewState() {

    class DefaultState(val contact: ProjectContact, val role: SabinaRoles) : ContactsViewState()
}