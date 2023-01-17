package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview.image_overview

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sabina.project.base.external.images.downloadFromUrlCropped
import com.sabina.project.base.external.models.SabinaRoles
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
import com.sabina.project.project_manager.databinding.FragmentImageOverviewBinding
import com.sabina.project.project_manager.domain.model.Project
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class ImageOverviewFragment : BaseFragment(R.layout.fragment_image_overview), BackPressListener {

    private val args: ImageOverviewFragmentArgs by navArgs()

    companion object {
        const val TAG = "ImageOverviewFragment"
        private const val RESULT_KEY = "RESULT_KEY"
        private const val DATA_KEY = "DATA_KEY"

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): Project = bundle.getParcelable(DATA_KEY)!!
    }

    @Inject
    lateinit var viewModelFactory: ImageOverviewViewModel.Factory
    private val viewModel: ImageOverviewViewModel by viewModels {
        ImageOverviewViewModel.provideFactory(
            _project = args.project,
            _image = args.image,
            _groupUuid = args.groupUuid,
            _objUuid = args.objUuid,
            assistedFactory = viewModelFactory
        )
    }

    private val binding by viewBinding(FragmentImageOverviewBinding::bind)
    private val navController by lazy { findNavController() }
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

    override fun onBackPressed() {
        viewModel.obtainAction(ImageOverviewViewActions.OnBackClick)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewsConfigurator.initStartState()
    }

    override fun onPause() {
        viewModel.obtainAction(ImageOverviewViewActions.SaveChangesOnExtraExit)
        super.onPause()
    }

    private fun onClose(result: Result, project: Project? = null) {
        val bundle = if (project == null)
            bundleOf(RESULT_KEY to result.name)
        else
            bundleOf(RESULT_KEY to result.name, DATA_KEY to project)
        navHostFragmentManager().setFragmentResult(TAG, bundle)
        navController.popBackStack()
    }

    private fun observeViewModel() {
        viewModel.viewState.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { state ->
            viewsConfigurator.renderState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.viewEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { event ->
            eventsConfigurator.handleViewEvent(event)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.navigateBack.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { pair ->
            onClose(pair.first, pair.second)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun saveInfo() {
        viewModel.obtainAction(
            ImageOverviewViewActions.SaveInfo(
                name = binding.etName.text.toString().trim(),
                comment = binding.etComment.text.toString().trim(),
            )
        )
    }

    private inner class ViewsConfigurator {
        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) { saveInfo() }
        }

        fun initStartState() {
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                viewModel.obtainAction(ImageOverviewViewActions.OnBackClick)
            }
            binding.ivImage.clicksWithDebounce {
                viewModel.obtainAction(ImageOverviewViewActions.OpenFullScreenImage)
            }
            binding.btnCamera.clicksWithDebounce {
                viewModel.obtainAction(ImageOverviewViewActions.OpenCamera)
            }
            binding.btnGallery.clicksWithDebounce {
                viewModel.obtainAction(ImageOverviewViewActions.OpenGallery)
            }
            binding.ivDeleteImage.clicksWithDebounce {
                viewModel.obtainAction(ImageOverviewViewActions.DeleteImage)
            }
            binding.tilCoordinates.setEndIconOnClickListener {
                viewModel.obtainAction(ImageOverviewViewActions.OpenMap)
            }
        }

        fun renderState(state: ImageOverviewViewState) {
            when (state) {
                is ImageOverviewViewState.Creating -> renderCreatingState()
                is ImageOverviewViewState.Overview -> renderOverviewState(state)
            }
        }

        private fun renderOverviewState(state: ImageOverviewViewState.Overview) {
            binding.tvPageName.text = getString(R.string.image_overview_toolbar_title_overview)
            binding.ivImage.downloadFromUrlCropped(state.image.link)

            binding.ivImage.isVisible = true
            binding.ivDeleteImage.isVisible = true
            binding.tilCoordinates.isVisible = true
            binding.tilComment.isVisible = true
            binding.tilName.isVisible = true
            binding.etName.setText(state.image.name)
            binding.etComment.setText(state.image.comment)

            if (state.image.geoPoint != null)
                binding.etCoordinates.setText(getString(R.string.map_coordinates, state.image.geoPoint!!.latitude, state.image.geoPoint!!.longitude))

            binding.etName.addTextChangedListener(textWatcher)
            binding.etComment.addTextChangedListener(textWatcher)

            if (state.role == SabinaRoles.REVIEWER) {
                binding.ivDeleteImage.isVisible = false
                binding.btnCamera.isVisible = false
                binding.btnGallery.isVisible = false
                if (state.image.geoPoint == null)
                    binding.tilCoordinates.setEndIconOnClickListener(null)

                binding.etComment.isEnabled = false
                binding.etName.isEnabled = false
            }
        }

        private fun renderCreatingState() {
            binding.tvPageName.text = getString(R.string.image_overview_toolbar_title_creating)

            binding.ivImage.isVisible = false
            binding.ivDeleteImage.isVisible = false
            binding.tilCoordinates.isVisible = false
            binding.tilComment.isVisible = false
            binding.tilName.isVisible = false

            binding.etName.addTextChangedListener(textWatcher)
            binding.etComment.addTextChangedListener(textWatcher)
        }
    }

    private inner class EventsConfigurator {
        fun handleViewEvent(event: ViewEvent) {
            when (event) {
                is ViewEvent.Navigation -> handleScreen(event.screen)
                is ViewEvent.AlmagestSnackbar -> handleSnackbar(event)
                is ViewEvent.Loading -> handleLoading(event)
            }
        }

        private fun handleLoading(event: ViewEvent.Loading) {
            binding.loadingView.isVisible = event is ViewEvent.Loading.Enabled
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
            binding.loadingView.isVisible = false
            viewModel.viewEvent.setResultListener(this@ImageOverviewFragment)
            navController.navigateViaScreenRoute(screen)
        }
    }

    enum class Result {
        CANCELED,
        UPDATED;

        companion object {
            fun enumValueOf(name: String): Result {
                return values().first { it.name == name }
            }
        }
    }
}