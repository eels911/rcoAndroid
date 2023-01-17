package com.sabina.project.project_manager.presentation.images.gallery

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.sabina.project.project_manager.databinding.FragmentGalleryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
internal class GalleryFragment : BaseFragment(R.layout.fragment_gallery), BackPressListener {

    companion object {
        const val TAG = "GalleryFragment"
        const val RESULT_KEY = "RESULT_KEY"
        const val DATA_KEY = "DATA_KEY"
        private const val PERMISSIONS_REQUEST_CODE = 1002

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): String = bundle.getString(DATA_KEY)!!
    }

    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()
    private val navController by lazy { findNavController() }
    private val binding by viewBinding(FragmentGalleryBinding::bind)

    private val viewModel: GalleryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewsConfigurator.initStartState()
        observeViewModel()
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
        val hasPermissions = PermissionUtils.isGalleryPermissionGranted(requireContext())
        viewModel.obtainAction(GalleryViewActions.UpdateState(getAllImages()))
        viewModel.obtainAction(GalleryViewActions.UpdateStatus(hasPermissions))
    }

    override fun onBackPressed() {
        onClose()
    }

    private fun observeViewModel() {
        viewModel.viewState.flowWithLifecycle(viewLifecycleOwner.lifecycle).filterNotNull().onEach {
            viewsConfigurator.renderState(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.navigateBack.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            onClose(result = Result.SELECTED, it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.viewEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            eventsConfigurator.handleViewEvent(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.intentSender.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(intent)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onClose(result: Result = Result.CANCELED, image: String? = null) {
        val bundle = if (image == null)
            bundleOf(RESULT_KEY to result.name)
        else
            bundleOf(RESULT_KEY to result.name, DATA_KEY to image)
        navHostFragmentManager().setFragmentResult(TAG, bundle)
        navController.popBackStack()
    }

    @SuppressLint("Range")
    private fun getAllImages(): List<String> {
        val imageList = mutableListOf<String>()

        MediaStore.Images.Media.EXTERNAL_CONTENT_URI?.let {
            val projection = arrayOf(MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.DATA)
            val cursor: Cursor = requireContext().contentResolver.query(it, projection, null, null, null)!!
            while (cursor.moveToNext()) {
                imageList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)))
            }
            cursor.close()
        }
        return imageList.toList().reversed()
    }

    private inner class ViewsConfigurator {
        private val imageSelectorAdapter = GalleryAdapter(
            onClick = { image ->
                viewModel.obtainAction(GalleryViewActions.SelectImage(image))
            }
        )

        fun initStartState() {
            binding.tvPageName.text = getString(R.string.image_selector_toolbar_title)
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                onClose()
            }

            binding.ivPermission.clicksWithDebounce {
                requestPermission()
            }
        }

        fun renderState(state: GalleryViewState) {
            when (state) {
                is GalleryViewState.EmptyList -> renderEmptyListState(state)
                is GalleryViewState.FilledList -> renderFilledListState(state)
            }
        }

        private fun requestPermission() {
            if (shouldShowRequestPermissionRationale(PermissionUtils.getGalleryPermissionsArray().first())) {
                viewModel.obtainAction(GalleryViewActions.RationalPermissionRequest(getString(R.string.permission_storage)))
            } else {
                PermissionUtils.requestPermissions(
                    fragment = this@GalleryFragment,
                    permissions = PermissionUtils.getGalleryPermissionsArray(),
                    requestCode = PERMISSIONS_REQUEST_CODE
                )
            }
        }

        private fun renderEmptyListState(state: GalleryViewState.EmptyList) {
            binding.rvImages.isVisible = false
            if (!state.hasPermissions) {
                requestPermission()
                binding.tvEmptyList.isVisible = false
                binding.ivPermission.isVisible = true
                return
            }
            binding.tvEmptyList.isVisible = true
            binding.ivPermission.isVisible = false
        }

        private fun renderFilledListState(state: GalleryViewState.FilledList) {
            binding.ivPermission.isVisible = false
            binding.rvImages.isVisible = true
            binding.tvEmptyList.isVisible = false
            if (binding.rvImages.adapter == null) {
                binding.rvImages.setHasFixedSize(true)
                binding.rvImages.recycledViewPool.setMaxRecycledViews(0, 50)
                binding.rvImages.adapter = imageSelectorAdapter
            }
            imageSelectorAdapter.submitList(state.images)
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
            viewModel.viewEvent.setResultListener(this@GalleryFragment)
            navController.navigateViaScreenRoute(screen)
        }
    }

    enum class Result {
        CANCELED,
        SELECTED;

        companion object {
            fun enumValueOf(name: String): Result {
                return values().first { it.name == name }
            }
        }
    }
}