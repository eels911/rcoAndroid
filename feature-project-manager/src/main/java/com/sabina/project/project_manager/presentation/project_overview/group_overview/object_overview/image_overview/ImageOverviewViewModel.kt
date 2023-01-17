package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview.image_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.base.external.images.ImageUtils
import com.sabina.project.base.external.models.NotificationStatus
import com.sabina.project.base.external.providers.StringProvider
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.core_navigation.external.helpers.launchOnIO
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.domain.Interactor
import com.sabina.project.project_manager.domain.model.Project
import com.sabina.project.project_manager.domain.model.ProjectImage
import com.sabina.project.project_manager.presentation.CameraScreen
import com.sabina.project.project_manager.presentation.DeleteDialogScreen
import com.sabina.project.project_manager.presentation.GalleryScreen
import com.sabina.project.project_manager.presentation.MapScreen
import com.sabina.project.project_manager.presentation.dialogs.DeleteDialog
import com.sabina.project.project_manager.presentation.images.camera.CameraFragment
import com.sabina.project.project_manager.presentation.images.gallery.GalleryFragment
import com.sabina.project.project_manager.presentation.map.MapFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

internal class ImageOverviewViewModel @AssistedInject constructor(
    @Assisted private var project: Project,
    @Assisted("groupUuid") private var groupUuid: String,
    @Assisted("objUuid") private var objUuid: String,
    @Assisted private var image: ProjectImage,
    private val imageUtils: ImageUtils,
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()
    private val diffFinder = DiffFinder()
    private val backClickHandler = BackClickHandler()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
            CameraFragment.TAG -> {
                if (CameraFragment.getResult(data) == CameraFragment.Result.TAKEN) {
                    val url = CameraFragment.getData(data)
                    imageUtils.createBitmap(url) { createImage(it) }
                }
            }
            GalleryFragment.TAG -> {
                if (GalleryFragment.getResult(data) == GalleryFragment.Result.SELECTED) {
                    val url = CameraFragment.getData(data)
                    imageUtils.createBitmap(url) { createImage(it) }
                }
            }
            DeleteDialog.TAG -> {
                if (DeleteDialog.getResult(data) == DeleteDialog.Result.POSITIVE)
                    deleteImage()
            }
            MapFragment.TAG -> {
                if (MapFragment.getResult(data) != MapFragment.Result.UPDATED)
                    return@MutableResultFlow
                image.geoPoint = MapFragment.getData(data)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
        }
    }

    init {
        /**
         * на сервере всегда объект изображения вместе с фото. hasImage нужен, чтобы определять стейт экрана на фронте.
         */
        image.hasImage = !image.isNew
    }

    private val navigateBackMutable = MutableSharedFlow<Pair<ImageOverviewFragment.Result, Project?>>()
    val navigateBack = navigateBackMutable.asSharedFlow()

    val viewEvent: ResultFlow = viewEventMutable

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    fun obtainAction(action: ImageOverviewViewActions) {
        when (action) {
            is ImageOverviewViewActions.DeleteImage -> {
                emit(viewEventMutable, ViewEvent.Navigation(DeleteDialogScreen(R.string.image_overview_delete_dialog_title)))
            }
            is ImageOverviewViewActions.OpenFullScreenImage -> {
                if (viewState.value == ImageOverviewViewState.Creating)
                    return
                // todo
            }
            is ImageOverviewViewActions.OpenCamera -> {
                if (image.hasImage)
                    updateProject(false)
                emit(viewEventMutable, ViewEvent.Navigation(CameraScreen))
            }
            is ImageOverviewViewActions.OpenGallery -> {
                if (image.hasImage)
                    updateProject(false)
                emit(viewEventMutable, ViewEvent.Navigation(GalleryScreen))
            }
            is ImageOverviewViewActions.OnBackClick -> backClickHandler.handle()
            is ImageOverviewViewActions.SaveInfo -> with(image) {
                name = action.name
                comment = action.comment
            }
            is ImageOverviewViewActions.OpenMap -> {
                image.isNew = false
                updateProject(false)
                val mark = Gson().toJson(image.geoPoint)
                emit(viewEventMutable, ViewEvent.Navigation(MapScreen(mark)))
            }
            is ImageOverviewViewActions.SaveChangesOnExtraExit -> {
                if (viewEvent.isPendingExecution) return
                if (!image.hasImage) return
                if (diffFinder.hasNotChanges()) return
                updateProject(isBackPressed = false)
            }
        }
    }

    private fun createImage(byteArray: ByteArray) {
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.createImage(byteArray, image.uuid) },
            onSuccess = { link ->
                image.link = link
                val text = stringProvider.getString(R.string.image_overview_snack_success_downloaded)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                image.hasImage = true
                updateProject(false)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            },
            onError = {
                val text = it.message ?: stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private fun deleteImage() {
        project.objectGroupList = project.objectGroupList.toMutableList().apply {
            val group = find { it.uuid == groupUuid }!!
            group.objects = group.objects.toMutableList().apply {
                val obj = find { it.uuid == objUuid }!!
                obj.images = obj.images.toMutableList().apply {
                    find { it.uuid == image.uuid }?.let { remove(it) }
                }
            }
        }.toList()
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.updateProject(project) },
            onSuccess = {
                val text = stringProvider.getString(R.string.image_overview_snack_success_deleted)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                emit(navigateBackMutable, ImageOverviewFragment.Result.UPDATED to project)
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
        project.objectGroupList = project.objectGroupList.toMutableList().apply {
            val group = find { it.uuid == groupUuid }!!
            group.objects = group.objects.toMutableList().apply {
                val obj = find { it.uuid == objUuid }!!
                obj.images = obj.images.toMutableList().apply {
                    find { it.uuid == image.uuid }?.let { remove(it) }
                    add(image)
                }
            }
        }.toList()
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.updateProject(project) },
            onSuccess = {
                if (isBackPressed) {
                    val text = if (image.isNew)
                        stringProvider.getString(R.string.image_overview_snack_success_created)
                    else
                        stringProvider.getString(R.string.image_overview_snack_success_updated)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))

                    emit(navigateBackMutable, ImageOverviewFragment.Result.UPDATED to project)
                }
                image.isNew = false
            },
            onError = {
                val text = stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private inner class BackClickHandler {
        fun handle() {
            if (image.hasImage) {
                if (diffFinder.hasNotChanges()) {
                    emit(navigateBackMutable, ImageOverviewFragment.Result.CANCELED to null)
                    return
                }
                updateProject(isBackPressed = true)
            } else {
                showEmptyPhotoSnack()
            }
        }

        private fun showEmptyPhotoSnack() {
            if (viewState.value == ImageOverviewViewState.Creating)
                emit(navigateBackMutable, ImageOverviewFragment.Result.CANCELED to null)
            else {
                val text = stringProvider.getString(R.string.image_overview_snack_warning_enter_photo)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.WARNING))
            }
        }
    }

    private inner class DiffFinder {
        private var imageSaved = copyData()

        fun hasNotChanges(): Boolean = imageSaved == image

        private fun copyData(): ProjectImage = image.copy()
    }

    private inner class StateConfigurator {
        val role = interactor.getRole()
        fun defineFragmentState(): ImageOverviewViewState {
            return if (image.hasImage)
                ImageOverviewViewState.Overview(image, role)
            else
                ImageOverviewViewState.Creating
        }
    }

    companion object {
        fun provideFactory(
            _project: String,
            _groupUuid: String,
            _objUuid: String,
            _image: String,
            assistedFactory: Factory
        ): ViewModelProvider.Factory {
            val project: Project = Gson().fromJson(_project, Project::class.java)
            val image: ProjectImage = if (_image.isEmpty())
                ProjectImage(uuid = UUID.randomUUID().toString()).apply { isNew = true }
            else
                Gson().fromJson(_image, ProjectImage::class.java)

            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(project, _groupUuid, _objUuid, image) as T
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            project: Project,
            @Assisted("groupUuid") groupUuid: String,
            @Assisted("objUuid") objUuid: String,
            image: ProjectImage,
        ): ImageOverviewViewModel
    }
}