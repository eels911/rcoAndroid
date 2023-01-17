package com.sabina.project.project_manager.presentation.images.gallery

import androidx.lifecycle.ViewModel
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.project_manager.presentation.RationalPermissionDialogScreen
import com.sabina.project.project_manager.presentation.dialogs.RationalPermissionDialog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

internal class GalleryViewModel : ViewModel() {

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

    private val viewStateMutable = MutableStateFlow<GalleryViewState?>(null)
    val viewState = viewStateMutable.asStateFlow()

    private val navigateBackMutable = MutableSharedFlow<String>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    fun obtainAction(action: GalleryViewActions) {
        when (action) {
            is GalleryViewActions.SelectImage -> emit(navigateBackMutable, action.image)
            is GalleryViewActions.UpdateState -> {
                stateConfigurator.images = action.images
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is GalleryViewActions.UpdateStatus -> {
                stateConfigurator.hasPermissions = action.hasPermissions
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is GalleryViewActions.RationalPermissionRequest -> {
                emit(viewEventMutable, ViewEvent.Navigation(RationalPermissionDialogScreen(action.title)))
            }
        }
    }

    private inner class StateConfigurator {
        var images: List<String> = listOf()
        var hasPermissions: Boolean = false

        fun defineFragmentState(): GalleryViewState {
            return when (images.size) {
                0 -> GalleryViewState.EmptyList(hasPermissions)
                else -> GalleryViewState.FilledList(images)
            }
        }
    }
}