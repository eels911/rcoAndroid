package com.sabina.project.presentation.presentation

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.setupWithNavController
import com.sabina.project.R
import com.sabina.project.base.external.locale.LocaleUtils
import com.sabina.project.base.external.ui.AccessManager
import com.sabina.project.base.external.ui.BackPressListener
import com.sabina.project.base.external.ui.KeyboardManager
import com.sabina.project.core_navigation.external.Screen
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.obtainNavController
import com.sabina.project.databinding.ActivityMainBinding
import com.sabina.project.external.AlmagestSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.anko.itemsSequence
import java.util.*
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), KeyboardManager, AccessManager {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val navController by lazy { obtainNavController(R.id.navHost) }
    private var backPressed = 0L
    private var viewsConfigurator = ViewsConfigurator()
    private var eventsConfigurator = EventsConfigurator()

    private val currentVisibleFragment: Fragment?
        get() = supportFragmentManager.fragments[0].childFragmentManager.fragments.lastOrNull { it.isVisible }

    @Inject
    lateinit var viewModelFactory: ActivityViewModel.Factory
    private val viewModel: ActivityViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
        viewModel.obtainAction(ActivityViewActions.InitLanguage(LocaleUtils.getCurrentLocale(this).language))
    }

    private fun observeViewModel() {
        viewModel.viewState.filterNotNull().onEach { state ->
            viewsConfigurator.renderState(state)
        }.launchIn(lifecycleScope)
        viewModel.viewEvent.onEach { viewEvent ->
            eventsConfigurator.handleViewEvent(viewEvent)
        }.launchIn(lifecycleScope)
        viewModel.initLanguageEvent.onEach { language ->
            val config = resources.configuration
            val locale = Locale(language)
            Locale.setDefault(locale)
            config.setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                createConfigurationContext(config)
            resources.updateConfiguration(config, resources.displayMetrics)
            viewsConfigurator.initStartState()
            viewModel.obtainAction(ActivityViewActions.GetRole)
        }.launchIn(lifecycleScope)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onBackPressed() {
        if (onBackPressedConsumedByFragment()) return
        (this as? KeyboardManager)?.hideKeyboard()
        val countFragment = supportFragmentManager.backStackEntryCount
        var countChild = 0
        for (str in supportFragmentManager.fragments) {
            if (str.childFragmentManager.backStackEntryCount > 0) countChild++
        }
        if (countChild > 0) {
            for (fragment in supportFragmentManager.fragments) {
                if (fragment.childFragmentManager.backStackEntryCount >= 0)
                    fragment.childFragmentManager.popBackStack()
            }
        } else if (countFragment > 0) {
            supportFragmentManager.popBackStack()
        } else {
            if (backPressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
                finishAndRemoveTask()
                exitProcess(0)
            } else {
                Toast.makeText(this, getString(R.string.main_back_pressed), Toast.LENGTH_SHORT)
                    .show()
                backPressed = System.currentTimeMillis()
            }
        }
    }

    private fun onBackPressedConsumedByFragment(): Boolean {
        val fragment = currentVisibleFragment
        if (fragment is BackPressListener) {
            fragment.onBackPressed()
            return true
        }
        return false
    }

    override fun hideKeyboard() {
        (this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun showKeyboard(editText: EditText) {
        (this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun logout() {
        viewModel.obtainAction(ActivityViewActions.Logout)
    }

    override fun login(userId: String) {
        viewModel.obtainAction(ActivityViewActions.Login(userId))
    }

    private inner class ViewsConfigurator {

        fun renderState(state: ActivityViewState) {
            when (state) {
                is ActivityViewState.Auth -> renderAuthState(state)
                is ActivityViewState.LoggedIn -> renderLoggedInState(state)
            }
        }

        private fun renderLoggedInState(state: ActivityViewState.LoggedIn) {
            navController.setGraph(R.navigation.navigation_logged_in)
            binding.btmLoggedIn.isVisible = true
            binding.btmAuth.isVisible = false
        }

        private fun renderAuthState(state: ActivityViewState.Auth) {
            navController.setGraph(R.navigation.navigation_auth)
            binding.btmLoggedIn.isVisible = false
            binding.btmAuth.isVisible = true
        }

        fun initStartState() {
            setContentView(binding.root)
            binding.btmLoggedIn.menu.itemsSequence().forEachIndexed { index, item ->
                when (index) {
                    0 -> item.title = getString(R.string.bnv_projects)
                    1 -> item.title = getString(R.string.bnv_settings)
                }
            }
            binding.btmAuth.menu.itemsSequence().forEachIndexed { index, item ->
                when (index) {
                    0 -> item.title = getString(R.string.bnv_sign_in)
                    1 -> item.title = getString(R.string.bnv_sign_up)
                }
            }
            binding.btmAuth.setupWithNavController(navController)
            binding.btmLoggedIn.setupWithNavController(navController)
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

// todo object type spinner
// todo object fields adapter
// todo object statuses