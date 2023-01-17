package com.sabina.project.project_manager.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.base.external.ui.BaseFragment
import com.sabina.project.base.external.ui.KeyboardManager
import com.sabina.project.base.external.viewBInding.viewBinding
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.navigateViaScreenRoute
import com.sabina.project.external.AlmagestSnackbar
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.FragmentProjectListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class ProjectListFragment : BaseFragment(R.layout.fragment_project_list) {

    companion object {
        const val TAG = "ProjectListFragment"
    }

    private val binding by viewBinding(FragmentProjectListBinding::bind)
    private val navController by lazy { findNavController() }

    @Inject
    lateinit var viewModelFactory: ProjectListViewModel.Factory
    private val viewModel by navGraphViewModels<ProjectListViewModel>(R.id.graphProjectManager) {
        viewModelFactory
    }
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewsConfigurator.initStartState()
        viewModel.obtainAction(ProjectListViewActions.LoadInfo)
    }

    private fun observeViewModel() {
        viewModel.viewState.flowWithLifecycle(viewLifecycleOwner.lifecycle).filterNotNull().onEach { state ->
            viewsConfigurator.renderState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.viewEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { event ->
            eventsConfigurator.handleViewEvent(event)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private inner class ViewsConfigurator {
        private val projectAdapter = ProjectAdapter(
            onClick = { project ->
                viewModel.obtainAction(ProjectListViewActions.OpenProject(item = project))
            }
        )

        fun initStartState() {
            binding.ivSearchProject.clicksWithDebounce {
                viewModel.obtainAction(ProjectListViewActions.EnableSearch)
            }
            binding.ivCreateProject.clicksWithDebounce {
                viewModel.obtainAction(ProjectListViewActions.CreateProject)
            }
            binding.ivCancelSearch.clicksWithDebounce {
                viewModel.obtainAction(ProjectListViewActions.DisableSearch)
            }
            binding.tvEmptyList.clicksWithDebounce {
                viewModel.obtainAction(ProjectListViewActions.CreateProject)
            }
            binding.ivSort.clicksWithDebounce {
                viewModel.obtainAction(ProjectListViewActions.SortByCreateTime)
            }
            if (binding.rvProjects.adapter == null) {
                val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                binding.rvProjects.addItemDecoration(itemDecorator)
                binding.rvProjects.adapter = projectAdapter
                (binding.rvProjects.layoutManager as LinearLayoutManager).reverseLayout = true
                (binding.rvProjects.layoutManager as LinearLayoutManager).stackFromEnd = true
            }
        }

        fun renderState(state: ProjectListViewState) {
            when (state) {
                is ProjectListViewState.FilledList -> renderFilledListState(state)
                is ProjectListViewState.EmptyList -> renderEmptyListState(state)
            }
        }

        private fun renderEmptyListState(state: ProjectListViewState.EmptyList) {
            binding.ivSearchProject.isVisible = false
            binding.ivCancelSearch.isVisible = false
            binding.tilSearch.isVisible = false
            binding.ivSort.isVisible = false
            binding.ivCreateProject.isVisible = false
            binding.rvProjects.isVisible = false
            binding.tvEmptyList.isVisible = true

            configureByRole(state.role)
        }

        private fun renderFilledListState(state: ProjectListViewState.FilledList) {
            binding.rvProjects.isVisible = true
            binding.tvEmptyList.isVisible = false

            if (state.isSearchEnabled) {
                (requireActivity() as? KeyboardManager)?.showKeyboard(binding.etSearch)
                binding.ivSearchProject.isVisible = false
                binding.ivCreateProject.isVisible = false
                binding.tvPageName.isVisible = false
                binding.ivSort.isVisible = false
                binding.tilSearch.isVisible = true
                binding.ivCancelSearch.isVisible = true
            } else {
                (requireActivity() as? KeyboardManager)?.hideKeyboard()
                binding.ivSearchProject.isVisible = true
                binding.ivCreateProject.isVisible = true
                binding.tvPageName.isVisible = true
                binding.ivSort.isVisible = true
                binding.tilSearch.isVisible = false
                binding.ivCancelSearch.isVisible = false
                binding.etSearch.text?.clear()
            }

            projectAdapter.submitList(state.projects)

            binding.etSearch.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        val query = p0.toString()
                        val projects = if (query.isEmpty())
                            state.projects
                        else
                            state.projects.filter { it.name.contains(query) }
                        projectAdapter.submitList(projects)
                    }
                }
            )

            configureByRole(state.role)
        }

        private fun configureByRole(role: SabinaRoles) {
            if (role == SabinaRoles.REVIEWER) {
                binding.ivCreateProject.isVisible = false
                binding.tvEmptyList.isEnabled = false
                binding.tvEmptyList.setCompoundDrawables(null, null, null, null)
                binding.tvEmptyList.text = getString(R.string.project_list_empty_list_reviewer)
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
            viewModel.viewEvent.setResultListener(this@ProjectListFragment)
            navController.navigateViaScreenRoute(screen)
        }
    }
}