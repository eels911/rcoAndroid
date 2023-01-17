package com.sabina.project.project_manager.presentation.project_overview.group_overview

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
import com.sabina.project.project_manager.databinding.FragmentGroupOverviewBinding
import com.sabina.project.project_manager.domain.model.Project
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class GroupOverviewFragment : BaseFragment(R.layout.fragment_group_overview), BackPressListener {

    private val args: GroupOverviewFragmentArgs by navArgs()

    companion object {
        const val TAG = "GroupOverviewFragment"
        private const val RESULT_KEY = "RESULT_KEY"
        private const val DATA_KEY = "DATA_KEY"

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): Project = bundle.getParcelable(DATA_KEY)!!
    }

    @Inject
    lateinit var viewModelFactory: GroupOverviewViewModel.Factory
    private val viewModel: GroupOverviewViewModel by viewModels {
        GroupOverviewViewModel.provideFactory(
            _project = args.project,
            _group = args.group,
            assistedFactory = viewModelFactory
        )
    }

    private val binding by viewBinding(FragmentGroupOverviewBinding::bind)
    private val navController by lazy { findNavController() }
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

    override fun onBackPressed() {
        viewModel.obtainAction(GroupOverviewViewActions.OnBackClick)
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
        viewModel.obtainAction(GroupOverviewViewActions.SaveChangesOnExtraExit)
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
        viewModel.obtainAction(GroupOverviewViewActions.SaveInfo(binding.etName.text.toString().trim()))
    }

    private fun doActionAfterCheckRequiredFields(action: () -> Unit) {
        if (binding.tilName.error == null)
            action.invoke()
        else
            viewModel.obtainAction(GroupOverviewViewActions.ShowEmptyNameSnack)
    }

    private inner class ViewsConfigurator {
        val objectTypes by lazy { resources.getStringArray(R.array.object_types) }
        private val groupOverviewAdapter by lazy {
            GroupOverviewAdapter(objectTypes,
                onClick = { group, index ->
                    doActionAfterCheckRequiredFields {
                        viewModel.obtainAction(GroupOverviewViewActions.OpenObject(item = group, index = index))
                    }
                }
            )
        }

        fun initStartState() {
            binding.ivCreateObject.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(GroupOverviewViewActions.CreateObject)
                }
            }
            binding.ivDeleteGroup.clicksWithDebounce {
                viewModel.obtainAction(GroupOverviewViewActions.DeleteGroup)
            }
            binding.tvEmptyList.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(GroupOverviewViewActions.CreateObject)
                }
            }
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                viewModel.obtainAction(GroupOverviewViewActions.OnBackClick)
            }
        }

        fun renderState(state: GroupOverviewViewState) {
            when (state) {
                is GroupOverviewViewState.Overview -> renderOverviewState(state)
                is GroupOverviewViewState.Creating -> renderCreatingState()
            }
        }

        private fun renderCreatingState() {
            binding.tvPageName.text = getString(R.string.group_overview_toolbar_title_creating)
            binding.ivDeleteGroup.isVisible = false
            binding.ivCreateObject.isVisible = false
            binding.rvObjects.isVisible = false

            checkName(binding.etName.text.toString().trim())

            binding.etName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    val str = p0.toString().trim()
                    checkName(str)
                    saveInfo()
                }
            })
        }

        private fun checkName(name: String) {
            if (name.isEmpty()) {
                binding.tvEmptyList.isVisible = false
            } else {
                binding.tvEmptyList.isVisible = true
            }
        }

        private fun renderOverviewState(state: GroupOverviewViewState.Overview) {
            binding.tvPageName.text = getString(R.string.group_overview_toolbar_title_overview)
            binding.etName.setText(state.group.name)
            binding.ivDeleteGroup.isVisible = true

            if (state.group.objects.isEmpty()) {
                binding.tvEmptyList.isVisible = true
                binding.ivCreateObject.isVisible = false
                binding.rvObjects.isVisible = false
            } else {
                binding.tvEmptyList.isVisible = false
                binding.ivCreateObject.isVisible = true
                binding.rvObjects.isVisible = true
            }

            if (binding.rvObjects.adapter == null) {
                val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                binding.rvObjects.addItemDecoration(itemDecorator)
                binding.rvObjects.adapter = groupOverviewAdapter
                (binding.rvObjects.layoutManager as LinearLayoutManager).reverseLayout = true
                (binding.rvObjects.layoutManager as LinearLayoutManager).stackFromEnd = true
            }
            groupOverviewAdapter.submitList(state.group.objects)

            binding.etName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    val str = p0.toString().trim()
                    binding.tilName.error = if (str.isEmpty()) getString(R.string.project_overview_name_error) else null
                    saveInfo()
                }
            })

            if (state.role == SabinaRoles.REVIEWER) {
                binding.etName.isEnabled = false

                binding.ivCreateObject.isVisible = false
                binding.ivDeleteGroup.isVisible = false

                binding.tvEmptyList.isEnabled = false
                binding.tvEmptyList.setCompoundDrawables(null, null, null, null)
                binding.tvEmptyList.text = getString(R.string.object_list_empty_list_reviewer)
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
            viewModel.viewEvent.setResultListener(this@GroupOverviewFragment)
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