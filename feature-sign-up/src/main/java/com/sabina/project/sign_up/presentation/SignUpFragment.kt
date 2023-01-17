package com.sabina.project.sign_up.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.base.external.ui.BaseFragment
import com.sabina.project.base.external.viewBInding.viewBinding
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.external.AlmagestSnackbar
import com.sabina.project.sign_up.R
import com.sabina.project.sign_up.databinding.SignUpFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
internal class SignUpFragment : BaseFragment(R.layout.sign_up_fragment) {

    companion object {
        const val TAG = "SignInFragment"
    }

    @Inject
    lateinit var viewModelFactory: SignUpViewModel.Factory
    private val viewModel by navGraphViewModels<SignUpViewModel>(R.id.graphSignUp) {
        viewModelFactory
    }

    private val binding by viewBinding(SignUpFragmentBinding::bind)
    private val viewsConfigurator = ViewsConfigurator()
    private val eventsConfigurator = EventsConfigurator()

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
        viewModel.clearFieldsEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            binding.etEmail.setText("")
            binding.etPassword.setText("")
            binding.etPasswordRepeat.setText("")
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun checkFields() {
        binding.btnSignUp.isVisible = !(binding.etEmail.text.toString().trim().isEmpty() ||
                binding.etPassword.text.toString().trim().isEmpty() ||
                binding.etPasswordRepeat.text.toString().trim().isEmpty() ||
                binding.etPassword.text.toString() != binding.etPasswordRepeat.text.toString())
    }

    private inner class ViewsConfigurator {
        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                checkFields()
            }
        }

        fun renderState(state: SignUpViewState) {
            when (state) {
                is SignUpViewState.DefaultState -> renderDefaultState(state)
            }
        }

        fun initStartState() {
            binding.btnSignUp.clicksWithDebounce {
                viewModel.obtainAction(
                    SignUpViewActions.SignUp(
                        email = binding.etEmail.text.toString(),
                        password = binding.etPassword.text.toString()
                    )
                )
            }
            binding.etEmail.addTextChangedListener(textWatcher)
            binding.etPassword.addTextChangedListener(textWatcher)
            binding.etPasswordRepeat.addTextChangedListener(textWatcher)
        }

        private fun renderDefaultState(state: SignUpViewState.DefaultState) {
            checkFields()
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
        }
    }
}