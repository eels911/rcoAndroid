package com.sabina.project.project_manager.presentation.project_overview.group_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.base.external.models.NotificationStatus
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.base.external.providers.StringProvider
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.core_navigation.external.helpers.launchOnIO
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.domain.Interactor
import com.sabina.project.project_manager.domain.model.Project
import com.sabina.project.project_manager.domain.model.ProjectGroup
import com.sabina.project.project_manager.domain.model.ProjectObject
import com.sabina.project.project_manager.domain.model.ProjectObjectStatus
import com.sabina.project.project_manager.presentation.DeleteDialogScreen
import com.sabina.project.project_manager.presentation.ObjectOverviewScreen
import com.sabina.project.project_manager.presentation.dialogs.DeleteDialog
import com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview.ObjectOverviewFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import java.util.*

internal class GroupOverviewViewModel @AssistedInject constructor(
    @Assisted private var project: Project,
    @Assisted private var group: ProjectGroup,
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()
    private val backClickHandler = BackClickHandler()
    private val diffFinder = DiffFinder()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
            ObjectOverviewFragment.TAG -> {
                if (ObjectOverviewFragment.getResult(data) != ObjectOverviewFragment.Result.CANCELED) {
                    project = ObjectOverviewFragment.getData(data)
                    group = project.objectGroupList.find { it.uuid == group.uuid }!!
                }
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            DeleteDialog.TAG -> {
                if (DeleteDialog.getResult(data) == DeleteDialog.Result.POSITIVE)
                    deleteGroup()
            }
        }
    }
    val viewEvent: ResultFlow = viewEventMutable

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val navigateBackMutable = MutableSharedFlow<Pair<GroupOverviewFragment.Result, Project?>>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    fun obtainAction(action: GroupOverviewViewActions) {
        when (action) {
            is GroupOverviewViewActions.OnBackClick -> backClickHandler.handle()
            is GroupOverviewViewActions.ShowEmptyNameSnack -> {
                val text = stringProvider.getString(R.string.group_overview_snack_warning_enter_name)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.WARNING))
            }
            is GroupOverviewViewActions.SaveInfo -> {
                group.name = action.name
            }
            is GroupOverviewViewActions.DeleteGroup -> {
                emit(viewEventMutable, ViewEvent.Navigation(DeleteDialogScreen(R.string.group_overview_delete_dialog_title)))
            }
            is GroupOverviewViewActions.SaveChangesOnExtraExit -> {
                if (viewEvent.isPendingExecution) return
                if (group.name.isEmpty()) return
                if (diffFinder.hasNotChanges()) return
                updateProject(isBackPressed = false)
            }
            is GroupOverviewViewActions.CreateObject -> {
                group.isNew = false
                updateProject(false)
                val project = Gson().toJson(project)
                emit(viewEventMutable, ViewEvent.Navigation(ObjectOverviewScreen(project = project, groupUuid = group.uuid)))
            }
            is GroupOverviewViewActions.OpenObject -> {
                if (stateConfigurator.role == SabinaRoles.REVIEWER && action.item.status == ProjectObjectStatus.CREATED ||
                    stateConfigurator.role == SabinaRoles.CREATOR && action.item.status == ProjectObjectStatus.CHECKED
                ) {
                    val text = stringProvider.getString(R.string.group_overview_snack_warning_forbidden_for_role)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.WARNING))
                    return
                }

                group.isNew = false
                updateProject(false)
                val project = Gson().toJson(project)
                val obj = Gson().toJson(action.item)
                emit(viewEventMutable, ViewEvent.Navigation(ObjectOverviewScreen(project = project, groupUuid = group.uuid, obj = obj)))
            }
        }
    }

    private var hasBackPressed = false
    private fun updateProject(isBackPressed: Boolean) {
        if (hasBackPressed)
            return
        hasBackPressed = isBackPressed
        project.objectGroupList = project.objectGroupList.toMutableList().apply {
            find { it.uuid == group.uuid }?.let { remove(it) }
            add(group)
        }.toList()
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.updateProject(project) },
            onSuccess = {
                if (isBackPressed) {
                    val text = if (group.isNew)
                        stringProvider.getString(R.string.group_overview_snack_success_created)
                    else
                        stringProvider.getString(R.string.group_overview_snack_success_updated)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))

                    emit(navigateBackMutable, GroupOverviewFragment.Result.UPDATED to project)
                }
                group.isNew = false
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private fun deleteGroup() {
        project.objectGroupList = project.objectGroupList.toMutableList().apply {
            find { it.uuid == group.uuid }?.let { remove(it) }
        }.toList()
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.updateProject(project) },
            onSuccess = {
                val text = stringProvider.getString(R.string.group_overview_snack_success_deleted)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                emit(navigateBackMutable, GroupOverviewFragment.Result.UPDATED to project)
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private inner class BackClickHandler {
        fun handle() {
            if (group.name.isEmpty())
                showEmptyNameSnack()
            else {
                if (diffFinder.hasNotChanges()) {
                    emit(navigateBackMutable, GroupOverviewFragment.Result.CANCELED to null)
                    return
                }
                updateProject(isBackPressed = true)
            }
        }

        private fun showEmptyNameSnack() {
            if (viewState.value == GroupOverviewViewState.Creating)
                emit(navigateBackMutable, GroupOverviewFragment.Result.CANCELED to null)
            else {
                obtainAction(GroupOverviewViewActions.ShowEmptyNameSnack)
            }
        }
    }

    private inner class DiffFinder {
        private var groupSaved = copyData()

        fun hasNotChanges(): Boolean = groupSaved == group

        private fun copyData(): ProjectGroup = group.copy()
    }

    private inner class StateConfigurator {
        val role = interactor.getRole()
        private var objectList: List<ProjectObject> = listOf()
        fun defineFragmentState(): GroupOverviewViewState {
            prepareData()
            return if (group.isNew)
                GroupOverviewViewState.Creating
            else
                GroupOverviewViewState.Overview(group, role, objectList)
        }

        private fun prepareData() {
            if (role != SabinaRoles.CREATOR)
                objectList = group.objects.sortedWith(
                    compareBy<ProjectObject> {
                        it.status == ProjectObjectStatus.READY_FOR_REVIEW
                    }.thenBy {
                        it.status == ProjectObjectStatus.INCOMPLETE
                    }.thenBy {
                        it.status == ProjectObjectStatus.CREATED
                    }
                )
            else
                objectList = group.objects.sortedWith(
                    compareBy<ProjectObject> {
                        it.status == ProjectObjectStatus.INCOMPLETE
                    }.thenBy {
                        it.status == ProjectObjectStatus.CREATED
                    }.thenBy {
                        it.status == ProjectObjectStatus.READY_FOR_REVIEW
                    }
                )
        }
    }

    companion object {
        fun provideFactory(
            _project: String,
            _group: String,
            assistedFactory: Factory
        ): ViewModelProvider.Factory {
            val project: Project = Gson().fromJson(_project, Project::class.java)
            val group: ProjectGroup = if (_group.isEmpty())
                ProjectGroup(uuid = UUID.randomUUID().toString()).apply { isNew = true }
            else
                Gson().fromJson(_group, ProjectGroup::class.java)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(project, group) as T
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            project: Project,
            group: ProjectGroup,
        ): GroupOverviewViewModel
    }
}