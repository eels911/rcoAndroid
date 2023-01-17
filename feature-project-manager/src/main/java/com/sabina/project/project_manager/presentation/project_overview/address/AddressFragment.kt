package com.sabina.project.project_manager.presentation.project_overview.address

import android.os.Bundle
import android.text.Editable
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
import com.sabina.project.project_manager.databinding.FragmentAddressBinding
import com.sabina.project.project_manager.domain.model.Project
import com.sabina.project.project_manager.domain.model.ProjectAddress
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class AddressFragment : BaseFragment(R.layout.fragment_address), BackPressListener {

    private val args: AddressFragmentArgs by navArgs()

    companion object {
        const val TAG = "AddressFragment"
        private const val RESULT_KEY = "RESULT_KEY"
        private const val DATA_KEY = "DATA_KEY"

        fun getResult(bundle: Bundle): Result = Result.enumValueOf(bundle.getString(RESULT_KEY)!!)
        fun getData(bundle: Bundle): Project = bundle.getParcelable(DATA_KEY)!!
    }

    @Inject
    lateinit var viewModelFactory: AddressViewModel.Factory
    private val viewModel: AddressViewModel by viewModels {
        AddressViewModel.provideFactory(
            item = args.item,
            assistedFactory = viewModelFactory
        )
    }

    private val binding by viewBinding(FragmentAddressBinding::bind)
    private val navController by lazy { findNavController() }
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

    override fun onBackPressed() {
        viewModel.obtainAction(AddressViewActions.OnBackClick)
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
        viewModel.obtainAction(AddressViewActions.SaveChangesOnExtraExit)
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
            AddressViewActions.SaveInfo(
                building = binding.etBuilding.text.toString().trim(),
                street = binding.etStreet.text.toString().trim(),
                postCode = binding.etPostCode.text.toString().trim(),
                region = binding.etRegion.text.toString().trim(),
                city = binding.etCity.text.toString().trim(),
                country = binding.etCountry.text.toString().trim(),
            )
        )
    }

    private inner class ViewsConfigurator {
        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                saveInfo()
            }
        }
        private val searchWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.length < 2) {
                    binding.etSearch.setAdapter(null)
                } else {
                    viewModel.obtainAction(AddressViewActions.GetSuggestions(s.toString().trim()))
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }

        fun renderState(state: AddressViewState) {
            when (state) {
                is AddressViewState.DefaultState -> renderDefaultState(state)
            }
        }

        fun initStartState() {
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.toolbar.setNavigationOnClickListener {
                viewModel.obtainAction(AddressViewActions.OnBackClick)
            }

            binding.tilCoordinates.setEndIconOnClickListener {
                viewModel.obtainAction(AddressViewActions.OpenMap)
            }
        }

        private fun renderDefaultState(state: AddressViewState.DefaultState) {
            binding.etCoordinates.removeTextChangedListener(textWatcher)
            binding.etCountry.removeTextChangedListener(textWatcher)
            binding.etPostCode.removeTextChangedListener(textWatcher)
            binding.etCity.removeTextChangedListener(textWatcher)
            binding.etRegion.removeTextChangedListener(textWatcher)
            binding.etStreet.removeTextChangedListener(textWatcher)
            binding.etSearch.removeTextChangedListener(searchWatcher)

            binding.etBuilding.setText(state.address.building)
            binding.etCountry.setText(state.address.country)
            binding.etPostCode.setText(state.address.postCode)
            binding.etCity.setText(state.address.city)
            binding.etRegion.setText(state.address.region)
            binding.etStreet.setText(state.address.street)

            binding.etCoordinates.addTextChangedListener(textWatcher)
            binding.etCountry.addTextChangedListener(textWatcher)
            binding.etPostCode.addTextChangedListener(textWatcher)
            binding.etCity.addTextChangedListener(textWatcher)
            binding.etRegion.addTextChangedListener(textWatcher)
            binding.etStreet.addTextChangedListener(textWatcher)
            binding.etSearch.addTextChangedListener(searchWatcher)

            if (state.address.geoPoint != null)
                binding.etCoordinates.setText(getString(R.string.map_coordinates, state.address.geoPoint!!.latitude, state.address.geoPoint!!.longitude))

            if (state.role == SabinaRoles.REVIEWER) {
                binding.etBuilding.isEnabled = false
                binding.etCountry.isEnabled = false
                binding.etPostCode.isEnabled = false
                binding.etRegion.isEnabled = false
                binding.etCity.isEnabled = false
                binding.tilSearch.isVisible = false
                binding.etStreet.isEnabled = false

                if (state.address.geoPoint == null)
                    binding.tilCoordinates.setEndIconOnClickListener(null)
            }
            setSuggestions(state.suggestions)
        }

        private fun setSuggestions(suggestions: List<ProjectAddress>) {
            if (suggestions.isEmpty()) return
            val list = suggestions.map {
                    it.value
            }.toMutableList()
            list.remove(binding.etSearch.text.toString())
            val adapter = ArrayAdapter(requireContext(), R.layout.item_drop_down, list)
            binding.etSearch.setOnItemClickListener { parent, _, position, _ ->
                binding.etSearch.dismissDropDown()
                binding.etSearch.setAdapter(null)
                viewModel.obtainAction(AddressViewActions.SetAddress(suggestions[position]))
                binding.etSearch.setSelection(binding.etSearch.text.toString().length)
            }

            binding.etSearch.setAdapter(adapter)
            binding.etSearch.showDropDown()
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
            viewModel.viewEvent.setResultListener(this@AddressFragment)
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