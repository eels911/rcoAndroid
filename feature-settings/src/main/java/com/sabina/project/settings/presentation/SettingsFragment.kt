package com.sabina.project.settings.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.base.external.rx.clicksWithDebounce
import com.sabina.project.base.external.ui.AccessManager
import com.sabina.project.base.external.ui.BaseFragment
import com.sabina.project.base.external.viewBInding.viewBinding
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.navigateViaScreenRoute
import com.sabina.project.external.AlmagestSnackbar
import com.sabina.project.settings.R
import com.sabina.project.settings.databinding.SettingsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
internal class SettingsFragment : BaseFragment(R.layout.settings_fragment) {

    companion object {
        const val TAG = "SettingsFragment"
    }

    @Inject
    lateinit var viewModelFactory: SettingsViewModel.Factory
    private val viewModel by navGraphViewModels<SettingsViewModel>(R.id.graphSettings) {
        viewModelFactory
    }

    private val navController by lazy { findNavController() }
    private val binding by viewBinding(SettingsFragmentBinding::bind)
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
        viewModel.viewEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { viewEvent ->
            eventsConfigurator.handleViewEvent(viewEvent)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.logoutEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            (requireActivity() as AccessManager).logout()
        }.launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.recreateEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            requireActivity().recreate()
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private inner class ViewsConfigurator {
        fun renderState(state: SettingsViewState) {
            when (state) {
                is SettingsViewState.DefaultState -> renderDefaultState(state)
            }
        }

        fun initStartState() {
            binding.tvNightModeSystem.clicksWithDebounce {
                viewModel.obtainAction(SettingsViewActions.SelectScheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
            }
            binding.tvNightModeLight.clicksWithDebounce {
                viewModel.obtainAction(SettingsViewActions.SelectScheme(AppCompatDelegate.MODE_NIGHT_NO))
            }
            binding.tvNightModeDark.clicksWithDebounce {
                viewModel.obtainAction(SettingsViewActions.SelectScheme(AppCompatDelegate.MODE_NIGHT_YES))
            }
            binding.tvLocaleRu.clicksWithDebounce {
                viewModel.obtainAction(SettingsViewActions.SelectLanguage(SabinaLanguage.RU))
            }
            binding.tvLocaleEn.clicksWithDebounce {
                viewModel.obtainAction(SettingsViewActions.SelectLanguage(SabinaLanguage.EN))
            }
            binding.btnLogout.clicksWithDebounce {
                viewModel.obtainAction(SettingsViewActions.Logout)
            }
            binding.tvContactEmail.clicksWithDebounce {
                val mailIntent = Intent(Intent.ACTION_SENDTO)
                mailIntent.data = Uri.parse("mailto:")

                if (mailIntent.resolveActivity(requireActivity().packageManager) == null) {
                    viewModel.obtainAction(SettingsViewActions.ShowMessageError(getString(R.string.email_client_required)))
                } else {
                    mailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(binding.tvContactEmail.text.toString()))
                    mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_app))
                    startActivity(mailIntent)
                }
            }
        }

        private fun renderDefaultState(state: SettingsViewState.DefaultState) {
            binding.tvNightModeSystem.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_rectangle_left_border)
            binding.tvNightModeLight.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_rectangle_right_border)
            binding.tvNightModeDark.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_rectangle_center_border)

            setUnselectedState(binding.tvNightModeSystem)
            setUnselectedState(binding.tvNightModeLight)
            setUnselectedState(binding.tvNightModeDark)
            when (state.selectedScheme) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> setSelectedState(binding.tvNightModeSystem)
                AppCompatDelegate.MODE_NIGHT_NO -> setSelectedState(binding.tvNightModeLight)
                AppCompatDelegate.MODE_NIGHT_YES -> setSelectedState(binding.tvNightModeDark)
            }
            AppCompatDelegate.setDefaultNightMode(state.selectedScheme)

            setUnselectedState(binding.tvLocaleRu)
            setUnselectedState(binding.tvLocaleEn)
            when (state.locale) {
                SabinaLanguage.RU -> setSelectedState(binding.tvLocaleRu)
                SabinaLanguage.EN -> setSelectedState(binding.tvLocaleEn)
            }
        }

        private fun setSelectedState(textView: TextView) {
            textView.background.setTint(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        private fun setUnselectedState(textView: TextView) {
            textView.background.setTint(ContextCompat.getColor(requireContext(), R.color.colorOnBackground))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor))
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
            viewModel.viewEvent.setResultListener(this@SettingsFragment)
            navController.navigateViaScreenRoute(screen)
        }
    }
}