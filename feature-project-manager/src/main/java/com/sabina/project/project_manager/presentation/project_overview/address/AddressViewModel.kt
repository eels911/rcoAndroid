package com.sabina.project.project_manager.presentation.project_overview.address

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
import com.sabina.project.project_manager.domain.model.ProjectAddress
import com.sabina.project.project_manager.presentation.MapScreen
import com.sabina.project.project_manager.presentation.map.MapFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*

internal class AddressViewModel @AssistedInject constructor(
    @Assisted private val project: Project,
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()
    private val diffFinder = DiffFinder()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
            MapFragment.TAG -> {
                if (MapFragment.getResult(data) != MapFragment.Result.UPDATED)
                    return@MutableResultFlow
                project.address.geoPoint = MapFragment.getData(data)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
        }
    }
    val viewEvent: ResultFlow = viewEventMutable

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val navigateBackMutable = MutableSharedFlow<Pair<AddressFragment.Result, Project?>>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    fun obtainAction(action: AddressViewActions) {
        when (action) {
            is AddressViewActions.SetAddress -> {
                project.address = action.address
                stateConfigurator.addressSuggestions = listOf()
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is AddressViewActions.GetSuggestions -> getAddress(action.query)
            is AddressViewActions.OnBackClick -> {
                if (diffFinder.hasNotChanges()) {
                    emit(navigateBackMutable, AddressFragment.Result.CANCELED to null)
                    return
                }
                updateProject(isBackPressed = true)
            }
            is AddressViewActions.SaveInfo -> with(project.address) {
                building = action.building
                postCode = action.postCode
                country = action.country
                city = action.city
                region = action.region
                street = action.street
            }
            is AddressViewActions.SaveChangesOnExtraExit -> {
                if (viewEvent.isPendingExecution) return
                if (diffFinder.hasNotChanges()) return
                updateProject(isBackPressed = false)
            }
            is AddressViewActions.OpenMap -> {
                updateProject(false)
                val mark = Gson().toJson(project.address.geoPoint)
                emit(viewEventMutable, ViewEvent.Navigation(MapScreen(mark)))
            }
        }
    }

    private fun getAddress(query: String) {
        launchOnIO(
            request = { interactor.getAddress(query) },
            onSuccess = {
                stateConfigurator.addressSuggestions = it.suggestions
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            },
            onError = {
                val text = it.message ?: stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
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
                    val text = stringProvider.getString(R.string.project_address_snack_success_updated)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                    emit(navigateBackMutable, AddressFragment.Result.UPDATED to project)
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

        private fun copyData(): Project = project.copy(address = project.address.copy(geoPoint = project.address.geoPoint?.copy()))
    }

    private inner class StateConfigurator {
        val role = interactor.getRole()
        var addressSuggestions: List<ProjectAddress> = listOf()

        fun defineFragmentState(): AddressViewState {
            return AddressViewState.DefaultState(project.address, role, addressSuggestions)
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
        ): AddressViewModel
    }
}