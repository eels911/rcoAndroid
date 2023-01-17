package com.sabina.project.project_manager.presentation.project_overview.contacts

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
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.base.external.ui.BackPressListener
import com.sabina.project.base.external.ui.BaseFragment
import com.sabina.project.base.external.viewBInding.viewBinding
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.navHostFragmentManager
import com.sabina.project.core_navigation.external.helpers.navigateViaScreenRoute
import com.sabina.project.external.AlmagestSnackbar
import com.sabina.project.project_manager.R
import com.sabina.project.project_manager.databinding.FragmentContactsBinding
import com.sabina.project.project_manager.domain.model.Project
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class ContactsFragment : BaseFragment(R.layout.fragment_contacts), BackPressListener {

    private val args: ContactsFragmentArgs by navArgs()

    companion object {
        const val TAG = "ContactsFragment"
        private const val RESULT_KEY = "RESULT_KEY"
        private const val DATA_KEY = "DATA_KEY"

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): Project = bundle.getParcelable(DATA_KEY)!!
    }

    @Inject
    lateinit var viewModelFactory: ContactsViewModel.Factory
    private val viewModel: ContactsViewModel by viewModels {
        ContactsViewModel.provideFactory(
            item = args.item,
            assistedFactory = viewModelFactory
        )
    }

    private val binding by viewBinding(FragmentContactsBinding::bind)
    private val navController by lazy { findNavController() }
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

    override fun onBackPressed() {
        viewModel.obtainAction(ContactsViewActions.OnBackClick)
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
        viewModel.obtainAction(ContactsViewActions.SaveChangesOnExtraExit)
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
            ContactsViewActions.SaveInfo(
                email = binding.etEmail.text.toString().trim(),
                phone = binding.etPhone.text.toString().trim(),
                name = binding.etName.text.toString().trim()
            )
        )
    }

    private inner class ViewsConfigurator {
        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) { saveInfo() }
        }

        fun renderState(state: ContactsViewState) {
            when (state) {
                is ContactsViewState.DefaultState -> renderDefaultState(state)
            }
        }

        fun initStartState() {
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                viewModel.obtainAction(ContactsViewActions.OnBackClick)
            }
        }

        private fun renderDefaultState(state: ContactsViewState.DefaultState) {
            binding.etEmail.setText(state.contact.email)
            binding.etPhone.setText(state.contact.phone)
            binding.etName.setText(state.contact.name)

            binding.etEmail.addTextChangedListener(textWatcher)
            binding.etPhone.addTextChangedListener(textWatcher)
            binding.etName.addTextChangedListener(textWatcher)

            if (state.role == SabinaRoles.REVIEWER) {
                binding.etName.isEnabled = false
                binding.etPhone.isEnabled = false
                binding.etEmail.isEnabled = false
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
            viewModel.viewEvent.setResultListener(this@ContactsFragment)
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