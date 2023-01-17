package com.sabina.project.presentation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.base.external.providers.StringProvider
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.core_navigation.external.helpers.launchOnIO
import com.sabina.project.presentation.domain.Interactor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ActivityViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()

    private val viewStateMutable = MutableStateFlow<ActivityViewState?>(null)
    val viewState = viewStateMutable.asStateFlow()

    private val initLanguageEventMutable = MutableSharedFlow<String>()
    val initLanguageEvent = initLanguageEventMutable.asSharedFlow()

    private val viewEventMutable = MutableResultFlow { resultKey, data -> }
    val viewEvent: ResultFlow = viewEventMutable

    fun obtainAction(action: ActivityViewActions) {
        when (action) {
            is ActivityViewActions.GetRole -> getRole()
            is ActivityViewActions.Logout -> {
                interactor.signOut()
                interactor.clearSharedPref()
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            }
            is ActivityViewActions.Login -> {
                interactor.setUserId(action.userId)
                getRole()
            }
            is ActivityViewActions.InitLanguage -> {
                val language = if (interactor.getLanguage() == SabinaLanguage.UNKNOWN) {
                    val localLanguage = action.localLanguage
                    if (SabinaLanguage.RU.code == localLanguage)
                        interactor.setLanguage(SabinaLanguage.RU)
                    else
                        interactor.setLanguage(SabinaLanguage.EN)
                    localLanguage
                } else
                    interactor.getLanguage().code
                emit(initLanguageEventMutable, language)
            }
        }
    }

    private fun getRole() {
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.getRole() },
            onSuccess = {
                interactor.setRole(it)
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            },
            onError = {
                obtainAction(ActivityViewActions.Logout)
            }
        )
    }

    private inner class StateConfigurator {
        fun defineFragmentState(): ActivityViewState {
            return if (interactor.getUserId().isEmpty()) {
                ActivityViewState.Auth
            } else {
                ActivityViewState.LoggedIn
            }
        }
    }

    class Factory @Inject constructor(
        private val stringProvider: StringProvider,
        private val interactor: Interactor,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityViewModel(
                stringProvider,
                interactor,
            ) as T
        }
    }
}