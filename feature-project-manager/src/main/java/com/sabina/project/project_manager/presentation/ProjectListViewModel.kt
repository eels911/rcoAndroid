package com.sabina.project.project_manager.presentation

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
import com.sabina.project.project_manager.presentation.project_overview.ProjectOverviewFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject

internal class ProjectListViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
            ProjectOverviewFragment.TAG -> {
                when (ProjectOverviewFragment.getResult(data)) {
                    ProjectOverviewFragment.Result.CANCELED -> {
                    }
                    ProjectOverviewFragment.Result.UPDATED -> {
                        val updatedItem = ProjectOverviewFragment.getData(data)
                        val oldItem = stateConfigurator.projects.find { it.uuid == updatedItem.uuid }
                        if (oldItem == updatedItem)
                            return@MutableResultFlow
                        if (oldItem != null)
                            stateConfigurator.projects = stateConfigurator.projects.minus(oldItem)
                        stateConfigurator.projects = stateConfigurator.projects.plus(updatedItem)
                    }
                    ProjectOverviewFragment.Result.DELETED -> {
                        val deletedItem = ProjectOverviewFragment.getData(data)
                        val oldItem = stateConfigurator.projects.find { it.uuid == deletedItem.uuid }
                        if (oldItem != null)
                            stateConfigurator.projects = stateConfigurator.projects.minus(oldItem)
                    }
                }
                obtainAction(ProjectListViewActions.DisableSearch)
            }
        }
    }
    val viewEvent: ResultFlow = viewEventMutable

    private val viewStateMutable = MutableStateFlow<ProjectListViewState?>(null)
    val viewState = viewStateMutable.asStateFlow()

    fun obtainAction(action: ProjectListViewActions) {
        when (action) {
            is ProjectListViewActions.CreateProject -> {
                emit(viewEventMutable, ViewEvent.Navigation(ProjectOverviewScreen()))
            }
            is ProjectListViewActions.EnableSearch -> {
                stateConfigurator.isSearchAvailable = true
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is ProjectListViewActions.SortByCreateTime -> {
                stateConfigurator.isSortedByAscending = !stateConfigurator.isSortedByAscending
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is ProjectListViewActions.DisableSearch -> {
                stateConfigurator.isSearchAvailable = false
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is ProjectListViewActions.OpenProject -> {
                val project = Gson().toJson(action.item)
                emit(viewEventMutable, ViewEvent.Navigation(ProjectOverviewScreen(item = project)))
            }
            is ProjectListViewActions.LoadInfo -> getProjects()
        }
    }

    private fun getProjects() {
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.getProjects() },
            onSuccess = {
                stateConfigurator.projects = when (interactor.getRole()) {
                    SabinaRoles.CREATOR -> it.filter { project -> project.userId == interactor.getUserId() }
                    SabinaRoles.REVIEWER -> it
                    SabinaRoles.ADMIN -> it
                }
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private inner class StateConfigurator {
        var projects: List<Project> = listOf()
        var isSearchAvailable: Boolean = false
        var isSortedByAscending: Boolean = false
        val role = interactor.getRole()

        fun defineFragmentState(): ProjectListViewState {
            return when (projects.size) {
                0 -> ProjectListViewState.EmptyList(role)
                else -> ProjectListViewState.FilledList(
                    role = role,
                    projects = if (isSortedByAscending) projects.sortedBy { it.createAt } else projects.sortedByDescending { it.createAt },
                    isSearchEnabled = isSearchAvailable
                )
            }
        }
    }

    class Factory @Inject constructor(
        private val stringProvider: StringProvider,
        private val interactor: Interactor,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectListViewModel(
                stringProvider,
                interactor,
            ) as T
        }
    }
}