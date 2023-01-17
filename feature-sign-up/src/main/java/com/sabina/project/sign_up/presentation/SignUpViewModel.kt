package com.sabina.project.sign_up.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.base.external.models.NotificationStatus
import com.sabina.project.base.external.providers.StringProvider
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.core_navigation.external.helpers.launchOnIO
import com.sabina.project.sign_up.R
import com.sabina.project.sign_up.domain.Interactor
import kotlinx.coroutines.flow.*
import javax.inject.Inject

internal class SignUpViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val viewEventMutable = MutableResultFlow { resultKey, data -> }
    val viewEvent: ResultFlow = viewEventMutable

    private val clearFieldsMutableEvent = MutableSharedFlow<Unit>()
    val clearFieldsEvent = clearFieldsMutableEvent.asSharedFlow()

    fun obtainAction(action: SignUpViewActions) {
        when (action) {
            is SignUpViewActions.SignUp -> signUp(action.email, action.password)
        }
    }

    private fun signUp(email: String, password: String) {
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.signUp(email, password) },
            onSuccess = {
                if (it == null) {
                    val text = stringProvider.getString(R.string.sign_up_error)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
                } else {
                    interactor.addUserToList(it.uid)
                    emit(clearFieldsMutableEvent, Unit)
                    val text = stringProvider.getString(R.string.account_created)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.SUCCESS))
                }
                viewStateMutable.value = stateConfigurator.defineFragmentState()
            },
            onError = {
                val text = it.message ?: stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private inner class StateConfigurator {
        fun defineFragmentState(): SignUpViewState {
            return SignUpViewState.DefaultState()
        }
    }

    class Factory @Inject constructor(
        private val stringProvider: StringProvider,
        private val interactor: Interactor,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignUpViewModel(
                stringProvider,
                interactor,
            ) as T
        }
    }
}