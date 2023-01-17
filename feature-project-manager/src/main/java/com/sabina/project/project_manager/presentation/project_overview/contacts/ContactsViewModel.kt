package com.sabina.project.project_manager.presentation.project_overview.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.base.external.models.NotificationStatus
import com.sabina.project.base.external.providers.StringProvider
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.core_navigation.external.helpers.launchOnIO
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.domain.Interactor
import com.sabina.project.project_manager.domain.model.Project
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*

internal class ContactsViewModel @AssistedInject constructor(
    @Assisted private val project: Project,
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()
    private val diffFinder = DiffFinder()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
        }
    }
    val viewEvent: ResultFlow = viewEventMutable

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val navigateBackMutable = MutableSharedFlow<Pair<ContactsFragment.Result, Project?>>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    fun obtainAction(action: ContactsViewActions) {
        when (action) {
            is ContactsViewActions.OnBackClick -> {
                if (diffFinder.hasNotChanges()) {
                    emit(navigateBackMutable, ContactsFragment.Result.CANCELED to null)
                    return
                }
                updateProject(isBackPressed = true)
            }
            is ContactsViewActions.SaveInfo -> with(project.contact) {
                email = action.email
                phone = action.phone
                name = action.name
            }
            is ContactsViewActions.SaveChangesOnExtraExit -> {
                if (diffFinder.hasNotChanges()) return
                updateProject(isBackPressed = false)
            }
        }
    }

    private var hasBackPressed = false
    private fun updateProject(isBackPressed: Boolean) {
        if (hasBackPressed)
            return
        hasBackPressed = isBackPressed
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.updateProject(project) },
            onSuccess = {
                if (isBackPressed) {
                    val text = stringProvider.getString(R.string.project_contacts_snack_success_updated)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                    emit(navigateBackMutable, ContactsFragment.Result.UPDATED to project)
                }
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private inner class DiffFinder {
        private var projectSaved = copyData()

        fun hasNotChanges(): Boolean = projectSaved == project

        private fun copyData(): Project = project.copy(contact = project.contact.copy())
    }

    private inner class StateConfigurator {
        val role = interactor.getRole()
        fun defineFragmentState(): ContactsViewState {
            return ContactsViewState.DefaultState(project.contact, role)
        }
    }

    companion object {
        fun provideFactory(
            item: String,
            assistedFactory: Factory
        ): ViewModelProvider.Factory {
            val project: Project = Gson().fromJson(item, Project::class.java)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(project) as T
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            project: Project,
        ): ContactsViewModel
    }
}