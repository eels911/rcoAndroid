package com.sabina.project.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.base.external.models.NotificationStatus
import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.base.external.providers.StringProvider
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.settings.domain.Interactor
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import javax.inject.Inject

internal class SettingsViewModel @AssistedInject constructor(
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val logoutEventMutable = MutableSharedFlow<Unit>()
    val logoutEvent = logoutEventMutable.asSharedFlow()

    private val recreateEventMutable = MutableSharedFlow<Unit>()
    val recreateEvent = recreateEventMutable.asSharedFlow()

    private val viewEventMutable = MutableResultFlow { resultKey, data ->
        when (resultKey) {
            LogoutDialog.TAG -> {
                if (LogoutDialog.getResult(data) == LogoutDialog.Result.POSITIVE)
                    emit(logoutEventMutable, Unit)
            }
        }
    }
    val viewEvent: ResultFlow = viewEventMutable

    fun obtainAction(action: SettingsViewActions) {
        when (action) {
            is SettingsViewActions.SelectScheme -> {
                if (stateConfigurator.selectedScheme == action.selectedScheme) return
                stateConfigurator.selectedScheme = action.selectedScheme
                interactor.setColorScheme(action.selectedScheme)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is SettingsViewActions.SelectLanguage -> {
                if (stateConfigurator.selectedLanguage == action.language) return
                stateConfigurator.selectedLanguage = action.language
                interactor.setLanguage(action.language)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
                emit(recreateEventMutable, Unit)
            }
            is SettingsViewActions.Logout -> emit(viewEventMutable, ViewEvent.Navigation(LogoutDialog.NavScreen))
            is SettingsViewActions.ShowMessageError -> {
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = action.title, status = NotificationStatus.ERROR))
            }
        }
    }

    private inner class StateConfigurator {
        var selectedScheme: Int = interactor.getColorScheme()
        var selectedLanguage: SabinaLanguage = interactor.getLanguage()
        fun defineFragmentState(): SettingsViewState {
            return SettingsViewState.DefaultState(selectedScheme, selectedLanguage)
        }
    }

    class Factory @Inject constructor(
        private val stringProvider: StringProvider,
        private val interactor: Interactor,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                stringProvider,
                interactor,
            ) as T
        }
    }
}