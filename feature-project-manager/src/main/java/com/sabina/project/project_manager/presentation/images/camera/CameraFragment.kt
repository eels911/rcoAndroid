package com.sabina.project.project_manager.presentation.images.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Size
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sabina.project.base.external.images.downloadFromUrl
import com.sabina.project.base.external.permission.PermissionUtils
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.base.external.ui.BackPressListener
import com.sabina.project.base.external.ui.BaseFragment
import com.sabina.project.base.external.viewBInding.viewBinding
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.navHostFragmentManager
import com.sabina.project.core_navigation.external.helpers.navigateViaScreenRoute
import com.sabina.project.external.AlmagestSnackbar
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.FragmentCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.util.*

@AndroidEntryPoint
internal class CameraFragment : BaseFragment(R.layout.fragment_camera), BackPressListener {
    companion object {
        const val TAG = "CameraFragment"
        private const val RESULT_KEY = "RESULT_KEY"
        private const val DATA_KEY = "DATA_KEY"
        private const val PHOTO_RESOLUTION_WIDTH = 1600
        private const val PHOTO_RESOLUTION_HEIGHT = 1200
        private const val PERMISSIONS_REQUEST_CODE = 1001

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): String = bundle.getString(DATA_KEY)!!
    }

    private val photoFileName: String
        get() = "${System.currentTimeMillis()}.jpg"

    private val viewModel: CameraViewModel by viewModels()

    private val binding by viewBinding(FragmentCameraBinding::bind)
    private val navController by lazy { findNavController() }
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

    override fun onBackPressed() {
        onClose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewsConfigurator.initStartState()
    }

    override fun onStart() {
        super.onStart()
        updateStatuses()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_CODE)
            updateStatuses()
    }

    private fun updateStatuses() {
        val hasPermissions = PermissionUtils.isCameraPermissionGranted(requireContext())
        viewModel.obtainAction(CameraViewActions.UpdateStatus(hasPermissions))
    }

    private fun observeViewModel() {
        viewModel.viewState.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { state ->
            viewsConfigurator.renderState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.viewEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { event ->
            eventsConfigurator.handleViewEvent(event)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.navigateBack.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { url ->
            onClose(Result.TAKEN, url)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.intentSender.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(intent)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onClose(result: Result = Result.CANCELED, url: String? = null) {
        val bundle = if (url == null)
            bundleOf(RESULT_KEY to result.name)
        else
            bundleOf(RESULT_KEY to result.name, DATA_KEY to url)
        navHostFragmentManager().setFragmentResult(TAG, bundle)
        navController.popBackStack()
    }

    private inner class ViewsConfigurator {
        private val file: File
            get() = File(requireContext().cacheDir, photoFileName)
        private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

        @SuppressLint("RestrictedApi")
        private val imageCapture: ImageCapture = ImageCapture.Builder()
            .setMaxResolution(Size(PHOTO_RESOLUTION_WIDTH, PHOTO_RESOLUTION_HEIGHT))
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        fun initStartState() {
            binding.ivTakePhoto.clicksWithDebounce {
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(requireContext()),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            viewModel.obtainAction(CameraViewActions.TakePhoto(output.savedUri.toString()))
                        }
                    })
            }
            binding.ivClear.clicksWithDebounce {
                binding.ivPhoto.setImageDrawable(null)
                viewModel.obtainAction(CameraViewActions.ClearImage)
            }
            binding.ivPermission.clicksWithDebounce {
                requestPermission()
            }
            binding.ivAccept.clicksWithDebounce {
                viewModel.obtainAction(CameraViewActions.AcceptImage)
            }
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                onClose()
            }
        }

        private fun requestPermission() {
            if (shouldShowRequestPermissionRationale(PermissionUtils.getCameraPermissionsArray().first())) {
                viewModel.obtainAction(CameraViewActions.RationalPermissionRequest(getString(R.string.permission_camera)))
            } else {
                PermissionUtils.requestPermissions(
                    fragment = this@CameraFragment,
                    permissions = PermissionUtils.getCameraPermissionsArray(),
                    requestCode = PERMISSIONS_REQUEST_CODE
                )
            }
        }

        fun renderState(state: CameraViewState) {
            when (state) {
                is CameraViewState.Overview -> renderOverviewState(state)
                is CameraViewState.Preview -> renderPreviewState(state)
            }
        }

        private fun renderPreviewState(state: CameraViewState.Preview) {
            binding.ivClear.isVisible = false
            binding.ivAccept.isVisible = false
            binding.ivPhoto.isVisible = false

            if (!state.hasPermissions) {
                binding.ivTakePhoto.isVisible = false
                binding.cameraView.isVisible = false
                binding.ivPermission.isVisible = true
                requestPermission()
                return
            }
            binding.ivPermission.isVisible = false
            binding.ivTakePhoto.isVisible = true
            binding.cameraView.isVisible = true
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(binding.cameraView.surfaceProvider) }
                val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this@CameraFragment, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        }

        private fun renderOverviewState(state: CameraViewState.Overview) {
            binding.ivClear.isVisible = true
            binding.ivAccept.isVisible = true
            binding.ivPhoto.isVisible = true

            binding.ivTakePhoto.isVisible = false
            binding.cameraView.isVisible = false

            binding.ivPhoto.downloadFromUrl(state.url)
        }
    }

    private inner class EventsConfigurator {
        fun handleViewEvent(event: ViewEvent) {
            when (event) {
                is ViewEvent.Navigation -> handleScreen(event.screen)
                is ViewEvent.AlmagestSnackbar -> handleSnackbar(event)
                is ViewEvent.Loading -> Unit
            }
        }

        private fun handleSnackbar(event: ViewEvent.AlmagestSnackbar) {
            AlmagestSnackbar.Builder(binding.root.context)
                .setStatus(event.status.index)
                .setMessage(event.messageId)
                .setMessage(event.message)
                .setGravity(event.gravity)
                .setDelay(event.delayMillis)
                .setOnDismissCallback(event.onDismiss)
                .build()
                .show(binding.root)
        }

        private fun handleScreen(screen: Screen) {
            viewModel.viewEvent.setResultListener(this@CameraFragment)
            navController.navigateViaScreenRoute(screen)
        }
    }

    enum class Result {
        CANCELED,
        TAKEN;

        companion object {
            fun enumValueOf(name: String): Result {
                return values().first { it.name == name }
            }
        }
    }
}