package com.sabina.project.project_manager.presentation.project_overview

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
import com.sabina.project.project_manager.presentation.AddressScreen
import com.sabina.project.project_manager.presentation.ContactsScreen
import com.sabina.project.project_manager.presentation.DeleteDialogScreen
import com.sabina.project.project_manager.presentation.GroupOverviewScreen
import com.sabina.project.project_manager.presentation.dialogs.DeleteDialog
import com.sabina.project.project_manager.presentation.project_overview.address.AddressFragment
import com.sabina.project.project_manager.presentation.project_overview.contacts.ContactsFragment
import com.sabina.project.project_manager.presentation.project_overview.group_overview.GroupOverviewFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import java.util.*

internal class ProjectOverviewViewModel @AssistedInject constructor(
    @Assisted private var project: Project,
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()
    private val backClickHandler = BackClickHandler()
    private val diffFinder = DiffFinder()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
            GroupOverviewFragment.TAG -> {
                if (GroupOverviewFragment.getResult(data) != GroupOverviewFragment.Result.CANCELED)
                    project = GroupOverviewFragment.getData(data)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            AddressFragment.TAG -> {
                if (AddressFragment.getResult(data) != AddressFragment.Result.CANCELED)
                    project = AddressFragment.getData(data)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            ContactsFragment.TAG -> {
                if (ContactsFragment.getResult(data) != ContactsFragment.Result.CANCELED)
                    project = ContactsFragment.getData(data)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            DeleteDialog.TAG -> {
                when (DeleteDialog.getResult(data)) {
                    DeleteDialog.Result.NEGATIVE -> {
                    }
                    DeleteDialog.Result.POSITIVE -> {
                        deleteProject()
                    }
                }
            }
        }
    }
    val viewEvent: ResultFlow = viewEventMutable

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val navigateBackMutable = MutableSharedFlow<Pair<ProjectOverviewFragment.Result, Project?>>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    init {
        if (project.userId.isEmpty())
            project.userId = interactor.getUserId()
    }

    fun obtainAction(action: ProjectOverviewViewActions) {
        when (action) {
            is ProjectOverviewViewActions.ShowEmptyNameSnack -> {
                val text = stringProvider.getString(R.string.project_overview_snack_warning_enter_name)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.WARNING))
            }
            is ProjectOverviewViewActions.OnBackClick -> backClickHandler.handle()
            is ProjectOverviewViewActions.SaveInfo -> {
                project.name = action.name
            }
            is ProjectOverviewViewActions.DeleteProject -> {
                emit(viewEventMutable, ViewEvent.Navigation(DeleteDialogScreen(R.string.project_overview_delete_dialog_title)))
            }
            is ProjectOverviewViewActions.OpenContacts -> {
                project.isNew = false
                updateProject(false)
                val item = Gson().toJson(project)
                emit(viewEventMutable, ViewEvent.Navigation(ContactsScreen(item)))
            }
            is ProjectOverviewViewActions.OpenAddress -> {
                project.isNew = false
                updateProject(false)
                val item = Gson().toJson(project)
                emit(viewEventMutable, ViewEvent.Navigation(AddressScreen(item)))
            }
            is ProjectOverviewViewActions.SaveChangesOnExtraExit -> {
                if (viewEvent.isPendingExecution) return
                if (project.name.isEmpty()) return
                if (diffFinder.hasNotChanges()) return
                updateProject(isBackPressed = false)
            }
            is ProjectOverviewViewActions.CreateGroup -> {
                project.isNew = false
                updateProject(false)
                val project = Gson().toJson(project)
                emit(viewEventMutable, ViewEvent.Navigation(GroupOverviewScreen(project = project)))
            }
            is ProjectOverviewViewActions.OpenGroup -> {
                project.isNew = false
                updateProject(false)
                val project = Gson().toJson(project)
                val group = Gson().toJson(action.item)
                emit(viewEventMutable, ViewEvent.Navigation(GroupOverviewScreen(project = project, group = group)))
            }
        }
    }

    private var hasBackPressed = false
    private fun updateProject(isBackPressed: Boolean) {
        if (hasBackPressed) return
        hasBackPressed = isBackPressed
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.updateProject(project) },
            onSuccess = {
                if (isBackPressed) {
                    val text = if (project.isNew)
                        stringProvider.getString(R.string.project_overview_snack_success_created)
                    else
                        stringProvider.getString(R.string.project_overview_snack_success_updated)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))

                    emit(navigateBackMutable, ProjectOverviewFragment.Result.UPDATED to project)
                }
                project.isNew = false
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private fun deleteProject() {
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.deleteProject(project.uuid) },
            onSuccess = {
                val text = stringProvider.getString(R.string.project_overview_snack_success_deleted)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                emit(navigateBackMutable, ProjectOverviewFragment.Result.DELETED to project)
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private inner class BackClickHandler {
        fun handle() {
            if (project.name.isEmpty())
                showEmptyNameSnack()
            else {
                if (diffFinder.hasNotChanges()) {
                    emit(navigateBackMutable, ProjectOverviewFragment.Result.CANCELED to null)
                    return
                }
                updateProject(isBackPressed = true)
            }
        }

        private fun showEmptyNameSnack() {
            if (viewState.value == ProjectOverviewViewState.Creating)
                emit(navigateBackMutable, ProjectOverviewFragment.Result.CANCELED to null)
            else {
                obtainAction(ProjectOverviewViewActions.ShowEmptyNameSnack)
            }
        }
    }

    private inner class DiffFinder {
        private var projectSaved = copyData()

        fun hasNotChanges(): Boolean = projectSaved == project

        private fun copyData(): Project = project.copy()
    }

    private inner class StateConfigurator {
        val role = interactor.getRole()
        fun defineFragmentState(): ProjectOverviewViewState {
            return if (project.isNew)
                ProjectOverviewViewState.Creating
            else
                ProjectOverviewViewState.Overview(project, role)
        }
    }

    companion object {
        fun provideFactory(
            item: String,
            assistedFactory: Factory
        ): ViewModelProvider.Factory {
            val project: Project = if (item.isNotEmpty())
                Gson().fromJson(item, Project::class.java)
            else
                Project(uuid = UUID.randomUUID().toString(), userId = "", createAt = System.currentTimeMillis()).apply { isNew = true }
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
        ): ProjectOverviewViewModel
    }
}