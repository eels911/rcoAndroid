package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview

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
import com.sabina.project.project_manager.domain.model.ProjectObject
import com.sabina.project.project_manager.domain.model.ProjectObjectStatus
import com.sabina.project.project_manager.presentation.DeleteDialogScreen
import com.sabina.project.project_manager.presentation.ImageOverviewScreen
import com.sabina.project.project_manager.presentation.MapScreen
import com.sabina.project.project_manager.presentation.dialogs.DeleteDialog
import com.sabina.project.project_manager.presentation.map.MapFragment
import com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview.image_overview.ImageOverviewFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import java.util.*

internal class ObjectOverviewViewModel @AssistedInject constructor(
    @Assisted private var project: Project,
    @Assisted private var obj: ProjectObject,
    @Assisted private var groupUuid: String,
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()
    private val backClickHandler = BackClickHandler()
    private val diffFinder = DiffFinder()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
            ImageOverviewFragment.TAG -> {
                if (ImageOverviewFragment.getResult(data) != ImageOverviewFragment.Result.CANCELED) {
                    project = ImageOverviewFragment.getData(data)
                    obj = project.objectGroupList.find { it.uuid == groupUuid }!!.objects.find { it.uuid == obj.uuid }!!
                }
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            DeleteDialog.TAG -> {
                if (DeleteDialog.getResult(data) == DeleteDialog.Result.POSITIVE)
                    deleteGroup()
            }
            MapFragment.TAG -> {
                if (MapFragment.getResult(data) != MapFragment.Result.UPDATED)
                    return@MutableResultFlow
                obj.geoPoint = MapFragment.getData(data)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
        }
    }
    val viewEvent: ResultFlow = viewEventMutable

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val navigateBackMutable = MutableSharedFlow<Pair<ObjectOverviewFragment.Result, Project?>>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    fun obtainAction(action: ObjectOverviewViewActions) {
        when (action) {
            is ObjectOverviewViewActions.OnBackClick -> backClickHandler.handle()
            is ObjectOverviewViewActions.ShowEmptyNameSnack -> {
                val text = stringProvider.getString(R.string.object_overview_snack_warning_enter_name)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.WARNING))
            }
            is ObjectOverviewViewActions.ShowSelectTypeSnack -> {
                val text = stringProvider.getString(R.string.object_overview_snack_warning_enter_type)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.WARNING))
            }
            is ObjectOverviewViewActions.SelectType -> {
                obj.type = action.type
                stateConfigurator.updateSameTypeObjects()
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is ObjectOverviewViewActions.SaveInfo -> {
                obj.name = action.name
                if (action.type != -1)
                    obj.type = action.type
            }
            is ObjectOverviewViewActions.SaveChangesOnExtraExit -> {
                if (viewEvent.isPendingExecution) return
                if (obj.name.isEmpty() || obj.type == -1) return
                if (diffFinder.hasNotChanges()) return
                updateProject(isBackPressed = false)
            }
            is ObjectOverviewViewActions.OpenMap -> {
                obj.isNew = false
                updateProject(false)
                val mark = Gson().toJson(obj.geoPoint)
                emit(viewEventMutable, ViewEvent.Navigation(MapScreen(mark)))
            }
            is ObjectOverviewViewActions.DeleteObject -> {
                emit(viewEventMutable, ViewEvent.Navigation(DeleteDialogScreen(R.string.object_overview_delete_dialog_title)))
            }
            is ObjectOverviewViewActions.CreateImage -> {
                obj.isNew = false
                updateProject(false)
                val project = Gson().toJson(project)
                emit(viewEventMutable, ViewEvent.Navigation(ImageOverviewScreen(project = project, groupUuid = groupUuid, objUuid = obj.uuid)))
            }
            is ObjectOverviewViewActions.OpenImage -> {
                obj.isNew = false
                updateProject(false)
                val project = Gson().toJson(project)
                val image = Gson().toJson(action.item)
                emit(viewEventMutable, ViewEvent.Navigation(ImageOverviewScreen(project = project, groupUuid = groupUuid, objUuid = obj.uuid, image = image)))
            }
            is ObjectOverviewViewActions.SetStatus -> {
                if (obj.status == action.status) {
                    val text = stringProvider.getString(R.string.group_overview_snack_warning_already_has_this_status)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.WARNING))
                    return
                }
                when (action.status) {
                    ProjectObjectStatus.CHECKED -> {
                        val text = stringProvider.getString(R.string.object_overview_snack_success_status_checked)
                        emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                    }
                    ProjectObjectStatus.INCOMPLETE -> {
                        val text = stringProvider.getString(R.string.object_overview_snack_success_status_incomplete)
                        emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                    }
                    ProjectObjectStatus.READY_FOR_REVIEW -> {
                        val text = stringProvider.getString(R.string.object_overview_snack_success_status_ready_for_review)
                        emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                    }
                    else -> Unit
                }
                obj.status = action.status
                obj.whoSetStatus = interactor.getUserId()
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
        }
    }

    private var hasBackPressed = false
    private fun updateProject(isBackPressed: Boolean) {
        if (hasBackPressed)
            return
        hasBackPressed = isBackPressed
        project.objectGroupList = project.objectGroupList.toMutableList().apply {
            val group = find { it.uuid == groupUuid }!!
            group.objects = group.objects.toMutableList().apply {
                find { it.uuid == obj.uuid }?.let { remove(it) }
                add(obj)
            }
        }.toList()
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.updateProject(project) },
            onSuccess = {
                if (isBackPressed) {
                    val text = if (obj.isNew)
                        stringProvider.getString(R.string.object_overview_snack_success_created)
                    else
                        stringProvider.getString(R.string.object_overview_snack_success_updated)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))

                    emit(navigateBackMutable, ObjectOverviewFragment.Result.UPDATED to project)
                }
                obj.isNew = false
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private fun deleteGroup() {
        project.objectGroupList = project.objectGroupList.toMutableList().apply {
            val group = find { it.uuid == groupUuid }!!
            group.objects = group.objects.toMutableList().apply {
                find { it.uuid == obj.uuid }?.let { remove(it) }
            }
        }.toList()
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.updateProject(project) },
            onSuccess = {
                val text = stringProvider.getString(R.string.object_overview_snack_success_deleted)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                emit(navigateBackMutable, ObjectOverviewFragment.Result.UPDATED to project)
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private inner class BackClickHandler {
        fun handle() {
            if (obj.name.isEmpty() || obj.type == -1) {
                if (obj.name.isEmpty())
                    showEmptyNameSnack()
                else
                    showSelectTypeSnack()
            } else {
                if (diffFinder.hasNotChanges()) {
                    emit(navigateBackMutable, ObjectOverviewFragment.Result.CANCELED to null)
                    return
                }
                updateProject(isBackPressed = true)
            }
        }

        private fun showSelectTypeSnack() {
            if (viewState.value is ObjectOverviewViewState.Creating)
                emit(navigateBackMutable, ObjectOverviewFragment.Result.CANCELED to null)
            else {
                obtainAction(ObjectOverviewViewActions.ShowSelectTypeSnack)
            }
        }

        private fun showEmptyNameSnack() {
            if (viewState.value is ObjectOverviewViewState.Creating)
                emit(navigateBackMutable, ObjectOverviewFragment.Result.CANCELED to null)
            else {
                obtainAction(ObjectOverviewViewActions.ShowEmptyNameSnack)
            }
        }
    }

    private inner class DiffFinder {
        private var objectSaved = copyData()

        fun hasNotChanges(): Boolean = objectSaved == obj

        private fun copyData(): ProjectObject = obj.copy()
    }

    private inner class StateConfigurator {
        val role = interactor.getRole()
        var sameTypeObjects = createSameTypeObjectList()

        fun updateSameTypeObjects() {
            sameTypeObjects = createSameTypeObjectList()
        }

        private fun createSameTypeObjectList(): List<ProjectObject> {
            val objects = mutableListOf<ProjectObject>()
            project.objectGroupList.forEach { group ->
                group.objects.forEach {
                    if (it.type == obj.type && it.uuid != obj.uuid) {
                        it.groupUuid = group.uuid
                        objects.add(it)
                    }
                }
            }
            if (role != SabinaRoles.CREATOR)
                objects.sortedWith(
                    compareBy<ProjectObject> {
                        it.status == ProjectObjectStatus.READY_FOR_REVIEW
                    }.thenBy {
                        it.status == ProjectObjectStatus.INCOMPLETE
                    }.thenBy {
                        it.status == ProjectObjectStatus.CREATED
                    }
                )
            else
                objects.sortedWith(
                    compareBy<ProjectObject> {
                        it.status == ProjectObjectStatus.INCOMPLETE
                    }.thenBy {
                        it.status == ProjectObjectStatus.CREATED
                    }.thenBy {
                        it.status == ProjectObjectStatus.READY_FOR_REVIEW
                    }
                )
            return objects.toList()
        }

        fun defineFragmentState(): ObjectOverviewViewState {
            return if (obj.isNew)
                ObjectOverviewViewState.Creating(obj, sameTypeObjects)
            else
                ObjectOverviewViewState.Overview(obj, role, sameTypeObjects)
        }
    }

    companion object {
        fun provideFactory(
            _project: String,
            _obj: String,
            _groupUuid: String,
            assistedFactory: Factory
        ): ViewModelProvider.Factory {
            val project: Project = Gson().fromJson(_project, Project::class.java)
            val obj: ProjectObject = if (_obj.isEmpty())
                ProjectObject(uuid = UUID.randomUUID().toString()).apply { isNew = true }
            else
                Gson().fromJson(_obj, ProjectObject::class.java)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(project, obj, _groupUuid) as T
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            project: Project,
            obj: ProjectObject,
            _groupUuid: String,
        ): ObjectOverviewViewModel
    }
}