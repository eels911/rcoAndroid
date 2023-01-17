package com.sabina.project.project_manager.presentation.project_overview.group_overview.object_overview

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.sabina.project.project_manager.databinding.FragmentObjectOverviewBinding
import com.sabina.project.project_manager.domain.model.Project
import com.sabina.project.project_manager.domain.model.ProjectObjectStatus
import com.sabina.project.project_manager.presentation.project_overview.group_overview.GroupOverviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class ObjectOverviewFragment : BaseFragment(R.layout.fragment_object_overview), BackPressListener {

    private val args: ObjectOverviewFragmentArgs by navArgs()

    companion object {
        const val TAG = "ObjectOverviewFragment"
        private const val RESULT_KEY = "RESULT_KEY"
        private const val DATA_KEY = "DATA_KEY"

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): Project = bundle.getParcelable(DATA_KEY)!!
    }

    @Inject
    lateinit var viewModelFactory: ObjectOverviewViewModel.Factory
    private val viewModel: ObjectOverviewViewModel by viewModels {
        ObjectOverviewViewModel.provideFactory(
            _project = args.project,
            _obj = args.obj,
            _groupUuid = args.groupUuid,
            assistedFactory = viewModelFactory
        )
    }

    private val binding by viewBinding(FragmentObjectOverviewBinding::bind)
    private val navController by lazy { findNavController() }
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

    override fun onBackPressed() {
        viewModel.obtainAction(ObjectOverviewViewActions.OnBackClick)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewsConfigurator.initStartState()
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

    override fun onPause() {
        viewModel.obtainAction(ObjectOverviewViewActions.SaveChangesOnExtraExit)
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

    private fun saveInfo() {
        viewModel.obtainAction(
            ObjectOverviewViewActions.SaveInfo(
                name = binding.etName.text.toString().trim(),
                type = viewsConfigurator.objectTypes.indexOf(binding.etObjectType.text.toString())
            )
        )
    }

    private fun doActionAfterCheckRequiredFields(action: () -> Unit) {
        if (binding.tilName.error != null) {
            viewModel.obtainAction(ObjectOverviewViewActions.ShowEmptyNameSnack)
            return
        }
        if (viewsConfigurator.objectTypes.indexOf(binding.etObjectType.text.toString()) == -1) {
            viewModel.obtainAction(ObjectOverviewViewActions.ShowSelectTypeSnack)
            return
        }
        action.invoke()
    }

    private inner class ViewsConfigurator {
        val objectTypesAdapter by lazy { ArrayAdapter(requireContext(), R.layout.item_drop_down, objectTypes) }
        val objectTypes by lazy { resources.getStringArray(R.array.object_types) }
        private val groupOverviewAdapter by lazy { GroupOverviewAdapter(objectTypes) }
        private val nameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                checkFields()
                saveInfo()
            }
        }
        private val typeTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                checkFields()
                saveInfo()
            }
        }
        private val objectOverviewAdapter by lazy {
            ObjectOverviewAdapter(
                onClick = { image, index ->
                    doActionAfterCheckRequiredFields {
                        viewModel.obtainAction(ObjectOverviewViewActions.OpenImage(item = image, index = index))
                    }
                }
            )
        }

        fun initStartState() {
            if (binding.rvImages.adapter == null) {
                val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL)
                binding.rvImages.addItemDecoration(itemDecorator)
                binding.rvImages.adapter = objectOverviewAdapter
                (binding.rvImages.layoutManager as LinearLayoutManager).reverseLayout = true
                (binding.rvImages.layoutManager as LinearLayoutManager).stackFromEnd = true
            }
            if (binding.rvObjects.adapter == null) {
                val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                binding.rvObjects.addItemDecoration(itemDecorator)
                binding.rvObjects.adapter = groupOverviewAdapter
                (binding.rvObjects.layoutManager as LinearLayoutManager).reverseLayout = true
                (binding.rvObjects.layoutManager as LinearLayoutManager).stackFromEnd = true
            }
            binding.etObjectType.inputType = InputType.TYPE_NULL
            binding.etObjectType.setAdapter(objectTypesAdapter)
            binding.etObjectType.setOnItemClickListener { parent, _, position, _ ->
                viewModel.obtainAction(ObjectOverviewViewActions.SelectType(position))
            }
            binding.etObjectType.clicksWithDebounce {
                binding.etObjectType.setAdapter(objectTypesAdapter)
                if (binding.etObjectType.isPopupShowing)
                    binding.etObjectType.showDropDown()
                else
                    binding.etObjectType.dismissDropDown()
            }
            binding.ivCreateImage.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ObjectOverviewViewActions.CreateImage)
                }
            }
            binding.tvReadyForReview.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ObjectOverviewViewActions.SetStatus(ProjectObjectStatus.READY_FOR_REVIEW))
                }
            }
            binding.tvIncomplete.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ObjectOverviewViewActions.SetStatus(ProjectObjectStatus.INCOMPLETE))
                }
            }
            binding.tvChecked.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ObjectOverviewViewActions.SetStatus(ProjectObjectStatus.CHECKED))
                }
            }
            binding.ivDeleteObject.clicksWithDebounce {
                viewModel.obtainAction(ObjectOverviewViewActions.DeleteObject)
            }
            binding.tvEmptyList.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ObjectOverviewViewActions.CreateImage)
                }
            }
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                viewModel.obtainAction(ObjectOverviewViewActions.OnBackClick)
            }
            binding.tilCoordinates.setEndIconOnClickListener {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ObjectOverviewViewActions.OpenMap)
                }
            }
        }

        fun renderState(state: ObjectOverviewViewState) {
            when (state) {
                is ObjectOverviewViewState.Overview -> renderOverviewState(state)
                is ObjectOverviewViewState.Creating -> renderCreatingState(state)
            }
        }

        private fun renderCreatingState(state: ObjectOverviewViewState.Creating) {
            binding.tvPageName.text = getString(R.string.object_overview_toolbar_title_creating)
            binding.ivDeleteObject.isVisible = false
            binding.ivCreateImage.isVisible = false
            binding.rvImages.isVisible = false
            binding.rvObjects.isVisible = false
            binding.etObjectType.setText(if (state.obj.type == -1) getString(R.string.object_overview_type_default) else objectTypes[state.obj.type], false)
            binding.tvReadyForReview.isVisible = false
            binding.tvIncomplete.isVisible = false
            binding.tvChecked.isVisible = false

            checkFields()

            binding.etObjectType.addTextChangedListener(nameTextWatcher)
            binding.etName.addTextChangedListener(typeTextWatcher)
            groupOverviewAdapter.submitList(state.sameTypeObjects)
        }

        private fun checkFields() {
            if (binding.etName.text.toString().trim().isEmpty() || objectTypes.indexOf(binding.etObjectType.text.toString()) == -1) {
                binding.tvEmptyList.isVisible = false
                binding.tilCoordinates.isVisible = false
                binding.rvObjects.isVisible = false
                binding.tvEmptyListAnalogs.isVisible = false
                binding.tvAnalogs.isVisible = false
            } else {
                binding.tvEmptyList.isVisible = true
                binding.tilCoordinates.isVisible = true
                binding.rvObjects.isVisible = true
                binding.tvAnalogs.isVisible = true
                binding.tvEmptyListAnalogs.isVisible = groupOverviewAdapter.currentList.isEmpty()
            }
        }

        private fun renderOverviewState(state: ObjectOverviewViewState.Overview) {
            binding.tvPageName.text = getString(R.string.object_overview_toolbar_title_overview)
            binding.etName.setText(state.obj.name)
            binding.ivDeleteObject.isVisible = true
            binding.tilCoordinates.isVisible = true
            binding.rvObjects.isVisible = true

            binding.etObjectType.setText(objectTypes[state.obj.type], false)

            if (state.obj.images.isEmpty()) {
                binding.tvEmptyList.isVisible = true
                binding.ivCreateImage.isVisible = false
                binding.rvImages.isVisible = false
            } else {
                binding.tvEmptyList.isVisible = false
                binding.ivCreateImage.isVisible = true
                binding.rvImages.isVisible = true
            }

            if (state.obj.geoPoint != null)
                binding.etCoordinates.setText(getString(R.string.map_coordinates, state.obj.geoPoint!!.latitude, state.obj.geoPoint!!.longitude))
            objectOverviewAdapter.submitList(state.obj.images)
            groupOverviewAdapter.submitList(state.sameTypeObjects)
            binding.tvEmptyListAnalogs.isVisible = state.sameTypeObjects.isEmpty()

            binding.etObjectType.addTextChangedListener(typeTextWatcher)
            binding.etName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    val str = p0.toString().trim()
                    binding.tilName.error = if (str.isEmpty()) getString(R.string.project_overview_name_error) else null
                    saveInfo()
                }
            })

            if (state.obj.geoPoint != null)
                binding.etCoordinates.setText(getString(R.string.map_coordinates, state.obj.geoPoint!!.latitude, state.obj.geoPoint!!.longitude))

            if (state.role == SabinaRoles.REVIEWER) {
                binding.ivCreateImage.isVisible = false
                binding.ivDeleteObject.isVisible = false

                binding.tvEmptyList.isEnabled = false
                binding.tvEmptyList.setCompoundDrawables(null, null, null, null)
                binding.tvEmptyList.text = getString(R.string.image_list_empty_list_reviewer)

                if (state.obj.geoPoint == null)
                    binding.tilCoordinates.setEndIconOnClickListener(null)
                binding.etName.isEnabled = false
                binding.etObjectType.isEnabled = false
                binding.tilObjectType.isEnabled = false
                binding.tvReadyForReview.isVisible = false
                binding.tvIncomplete.isVisible = true
                binding.tvChecked.isVisible = true
            } else {
                binding.tvReadyForReview.isVisible = true
                binding.tvIncomplete.isVisible = false
                binding.tvChecked.isVisible = false
            }
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
            viewModel.viewEvent.setResultListener(this@ObjectOverviewFragment)
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