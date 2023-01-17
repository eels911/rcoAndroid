package com.sabina.project.project_manager.presentation.project_overview.contacts

internal sealed class ContactsViewActions {
    class SaveInfo(
        val email: String,
        val phone: String,
        val name: String,
    ) : ContactsViewActions()

    object OnBackClick : ContactsViewActions()
    object SaveChangesOnExtraExit : ContactsViewActions()
}