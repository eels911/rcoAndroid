package com.sabina.project.project_manager.presentation.project_overview

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
import com.sabina.project.base.external.ui.UtilsUI
import com.sabina.project.base.external.viewBInding.viewBinding
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.navHostFragmentManager
import com.sabina.project.core_navigation.external.helpers.navigateViaScreenRoute
import com.sabina.project.external.AlmagestSnackbar
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.FragmentProjectOverviewBinding
import com.sabina.project.project_manager.domain.model.Project
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class ProjectOverviewFragment : BaseFragment(R.layout.fragment_project_overview), BackPressListener {

    private val args: ProjectOverviewFragmentArgs by navArgs()

    companion object {
        const val TAG = "ProjectOverviewFragment"
        private const val RESULT_KEY = "RESULT_KEY"
        private const val DATA_KEY = "DATA_KEY"

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): Project = bundle.getParcelable(DATA_KEY)!!
    }

    @Inject
    lateinit var viewModelFactory: ProjectOverviewViewModel.Factory
    private val viewModel: ProjectOverviewViewModel by viewModels {
        ProjectOverviewViewModel.provideFactory(
            item = args.item,
            assistedFactory = viewModelFactory
        )
    }

    private val binding by viewBinding(FragmentProjectOverviewBinding::bind)
    private val navController by lazy { findNavController() }
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

    override fun onBackPressed() {
        viewModel.obtainAction(ProjectOverviewViewActions.OnBackClick)
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
        viewModel.obtainAction(ProjectOverviewViewActions.SaveChangesOnExtraExit)
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
        viewModel.obtainAction(ProjectOverviewViewActions.SaveInfo(binding.etName.text.toString().trim()))
    }

    private fun doActionAfterCheckRequiredFields(action: () -> Unit) {
        if (binding.tilName.error == null)
            action.invoke()
        else
            viewModel.obtainAction(ProjectOverviewViewActions.ShowEmptyNameSnack)
    }

    private inner class ViewsConfigurator {

        private val projectOverviewAdapter = ProjectOverviewAdapter(
            onClick = { group, index ->
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ProjectOverviewViewActions.OpenGroup(item = group, index = index))
                }
            }
        )

        fun initStartState() {
            binding.ivCreateGroup.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ProjectOverviewViewActions.CreateGroup)
                }
            }
            binding.tvEmptyList.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ProjectOverviewViewActions.CreateGroup)
                }
            }
            binding.btnAddress.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ProjectOverviewViewActions.OpenAddress)
                }
            }
            binding.btnContacts.clicksWithDebounce {
                doActionAfterCheckRequiredFields {
                    viewModel.obtainAction(ProjectOverviewViewActions.OpenContacts)
                }
            }
            binding.ivDeleteProject.clicksWithDebounce {
                viewModel.obtainAction(ProjectOverviewViewActions.DeleteProject)
            }
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                viewModel.obtainAction(ProjectOverviewViewActions.OnBackClick)
            }
        }

        fun renderState(state: ProjectOverviewViewState) {
            when (state) {
                is ProjectOverviewViewState.Overview -> renderOverviewState(state)
                is ProjectOverviewViewState.Creating -> renderCreatingState()
            }
        }

        private fun renderCreatingState() {
            binding.tvPageName.text = getString(R.string.project_overview_toolbar_title_creating)
            binding.ivDeleteProject.isVisible = false
            binding.ivCreateGroup.isVisible = false
            binding.rvGroups.isVisible = false
            binding.tvCreateAt.isVisible = false

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
                binding.btnAddress.isVisible = false
                binding.btnContacts.isVisible = false
                binding.tvEmptyList.isVisible = false
            } else {
                binding.btnAddress.isVisible = true
                binding.btnContacts.isVisible = true
                binding.tvEmptyList.isVisible = true
            }
        }

        private fun renderOverviewState(state: ProjectOverviewViewState.Overview) {
            binding.tvPageName.text = getString(R.string.project_overview_toolbar_title_overview)
            binding.etName.setText(state.project.name)
            binding.tvCreateAt.isVisible = true
            binding.ivDeleteProject.isVisible = true
            binding.tvCreateAt.text = getString(R.string.project_overview_create_at, UtilsUI.convertTimestampToDate(state.project.createAt))

            if (state.project.objectGroupList.isEmpty()) {
                binding.tvEmptyList.isVisible = true
                binding.ivCreateGroup.isVisible = false
                binding.rvGroups.isVisible = false
            } else {
                binding.tvEmptyList.isVisible = false
                binding.ivCreateGroup.isVisible = true
                binding.rvGroups.isVisible = true
            }

            if (binding.rvGroups.adapter == null) {
                val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                binding.rvGroups.addItemDecoration(itemDecorator)
                binding.rvGroups.adapter = projectOverviewAdapter
                (binding.rvGroups.layoutManager as LinearLayoutManager).reverseLayout = true
                (binding.rvGroups.layoutManager as LinearLayoutManager).stackFromEnd = true
            }
            projectOverviewAdapter.submitList(state.project.objectGroupList)

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

                binding.ivCreateGroup.isVisible = false
                binding.ivDeleteProject.isVisible = false

                binding.tvEmptyList.isEnabled = false
                binding.tvEmptyList.setCompoundDrawables(null, null, null, null)
                binding.tvEmptyList.text = getString(R.string.group_list_empty_list_reviewer)
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
            viewModel.viewEvent.setResultListener(this@ProjectOverviewFragment)
            navController.navigateViaScreenRoute(screen)
        }
    }

    enum class Result {
        CANCELED,
        DELETED,
        UPDATED;

        companion object {
            fun enumValueOf(name: String): Result {
                return values().first { it.name == name }
            }
        }
    }
}