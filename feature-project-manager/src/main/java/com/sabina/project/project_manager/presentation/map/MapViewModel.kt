package com.sabina.project.project_manager.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.base.external.extensions.getRoundedGeo
import com.sabina.project.base.external.map.LocationManager
import com.sabina.project.base.external.models.NotificationStatus
import com.sabina.project.base.external.models.SabinaGeoPoint
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.base.external.providers.StringProvider
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.domain.Interactor
import com.sabina.project.project_manager.presentation.RationalPermissionDialogScreen
import com.sabina.project.project_manager.presentation.dialogs.RationalPermissionDialog
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

internal class MapViewModel @AssistedInject constructor(
    @Assisted private val oldMark: SabinaGeoPoint?,
    private val locationManager: LocationManager,
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
            RationalPermissionDialog.TAG -> {
                if (RationalPermissionDialog.getResult(data) == RationalPermissionDialog.Result.POSITIVE) {
                    emit(intentSenderMutable, Unit)
                }
            }
        }
    }
    val viewEvent: ResultFlow = viewEventMutable

    private val viewStateMutable = MutableStateFlow<MapViewState?>(null)
    val viewState = viewStateMutable.asStateFlow()

    private val navigateBackMutable = MutableSharedFlow<Pair<MapFragment.Result, SabinaGeoPoint?>>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    private val intentSenderMutable = MutableSharedFlow<Unit>()
    val intentSender = intentSenderMutable.asSharedFlow()

    fun obtainAction(action: MapViewActions) {
        when (action) {
            is MapViewActions.OnBackClick -> {
                emit(navigateBackMutable, MapFragment.Result.CANCELED to null)
            }
            is MapViewActions.PutMark -> {
                if (interactor.getRole() == SabinaRoles.REVIEWER) return
                stateConfigurator.mark = action.mark
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is MapViewActions.PutUserGeolocationMark -> {
                stateConfigurator.userGeolocationMark = action.userGeolocationMark
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is MapViewActions.SaveMark -> {
                val sabinaGeoPoint = SabinaGeoPoint(stateConfigurator.mark!!.latitude.getRoundedGeo(), stateConfigurator.mark!!.longitude.getRoundedGeo())
                if (sabinaGeoPoint == oldMark)
                    emit(navigateBackMutable, MapFragment.Result.CANCELED to null)
                else {
                    emit(navigateBackMutable, MapFragment.Result.UPDATED to sabinaGeoPoint)
                }
            }
            is MapViewActions.UpdateStatus -> {
                stateConfigurator.isGpsEnabled = action.isGpsEnabled
                stateConfigurator.hasPermissions = action.hasPermissions
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is MapViewActions.LoadLocation -> {
                locationManager.loadLocation {
                    if (it == null) {
                        val text = stringProvider.getString(R.string.map_snack_error_location)
                        emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
                    } else {
                        action.onSuccess.invoke(it)
                    }
                }
            }
            MapViewActions.RationalPermissionRequest -> {
                val title = stringProvider.getString(R.string.permission_geo)
                emit(viewEventMutable, ViewEvent.Navigation(RationalPermissionDialogScreen(title)))
            }
        }
    }

    private inner class StateConfigurator {
        var isGpsEnabled = false
        var hasPermissions = false
        var mark: SabinaGeoPoint? = oldMark
        var userGeolocationMark: SabinaGeoPoint? = null

        fun defineFragmentState(): MapViewState {
            return MapViewState.DefaultState(
                isGpsEnabled = isGpsEnabled,
                hasPermissions = hasPermissions,
                userGeolocationMark = userGeolocationMark,
                mark = mark
            )
        }
    }

    companion object {
        fun provideFactory(
            item: String,
            assistedFactory: Factory
        ): ViewModelProvider.Factory {
            var mark: SabinaGeoPoint? = null
            if (item.isNotEmpty())
                mark = Gson().fromJson(item, SabinaGeoPoint::class.java)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(mark) as T
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            oldMark: SabinaGeoPoint?
        ): MapViewModel
    }
}