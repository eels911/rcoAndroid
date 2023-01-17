package com.sabina.project.project_manager.presentation.project_overview.address

import com.sabina.project.project_manager.domain.model.ProjectAddress

internal sealed class AddressViewActions {
    class SaveInfo(
        val building: String,
        val postCode: String,
        val street: String,
        val city: String,
        val region: String,
        val country: String,
    ) : AddressViewActions()

    object OnBackClick : AddressViewActions()
    class GetSuggestions(val query: String) : AddressViewActions()
    class SetAddress(val address: ProjectAddress) : AddressViewActions()
    object SaveChangesOnExtraExit : AddressViewActions()

    object OpenMap : AddressViewActions()
}