package com.sabina.project.project_manager.presentation.images.camera

import androidx.lifecycle.ViewModel
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.project_manager.presentation.RationalPermissionDialogScreen
import com.sabina.project.project_manager.presentation.dialogs.RationalPermissionDialog
import kotlinx.coroutines.flow.*

internal class CameraViewModel : ViewModel() {

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

    private val intentSenderMutable = MutableSharedFlow<Unit>()
    val intentSender = intentSenderMutable.asSharedFlow()

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val navigateBackMutable = MutableSharedFlow<String>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    fun obtainAction(action: CameraViewActions) {
        when (action) {
            is CameraViewActions.TakePhoto -> {
                stateConfigurator.url = action.url
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is CameraViewActions.AcceptImage -> {
                emit(navigateBackMutable, stateConfigurator.url!!)
            }
            is CameraViewActions.ClearImage -> {
                stateConfigurator.url = null
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is CameraViewActions.UpdateStatus -> {
                stateConfigurator.hasPermissions = action.hasPermissions
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is CameraViewActions.RationalPermissionRequest -> {
                emit(viewEventMutable, ViewEvent.Navigation(RationalPermissionDialogScreen(action.title)))
            }
        }
    }

    private inner class StateConfigurator {
        var url: String? = null
        var hasPermissions: Boolean = false

        fun defineFragmentState(): CameraViewState {
            return if (url == null)
                CameraViewState.Preview(hasPermissions)
            else
                CameraViewState.Overview(url!!)
        }
    }
}