package com.sabina.project.sign_in.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseUser
import com.sabina.project.base.external.extensions.emit
import com.sabina.project.base.external.models.NotificationStatus
import com.sabina.project.base.external.providers.StringProvider
import com.sabina.project.core_navigation.external.ViewEvent
import com.sabina.project.core_navigation.external.helpers.MutableResultFlow
import com.sabina.project.core_navigation.external.helpers.ResultFlow
import com.sabina.project.core_navigation.external.helpers.launchOnIO
import com.sabina.project.sign_in.R
import com.sabina.project.sign_in.domain.Interactor
import kotlinx.coroutines.flow.*
import javax.inject.Inject

internal class SignInViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    private val interactor: Interactor,
) : ViewModel() {

    private val stateConfigurator = StateConfigurator()

    private val viewStateMutable = MutableStateFlow(stateConfigurator.defineFragmentState())
    val viewState = viewStateMutable.asStateFlow()

    private val viewEventMutable = MutableResultFlow { resultKey, data -> }
    val viewEvent: ResultFlow = viewEventMutable

    private val signedInMutableEvent = MutableSharedFlow<FirebaseUser>()
    val signedInEvent = signedInMutableEvent.asSharedFlow()

    fun obtainAction(action: SignInViewActions) {
        when (action) {
            is SignInViewActions.SignIn -> signIn(action.email, action.password)
        }
    }

    private fun signIn(email: String, password: String) {
        launchOnIO(
            loader = viewEventMutable,
            request = { interactor.signIn(email, password) },
            onSuccess = { user ->
                if (user == null) {
                    val text = stringProvider.getString(R.string.sign_up_error)
                    emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
                } else {
                    emit(signedInMutableEvent, user)
                }
            },
            onError = {
                val text = it.message ?: stringProvider.getString(R.string.default_error_message)
                emit(viewEventMutable, ViewEvent.AlmagestSnackbar(message = text, status = NotificationStatus.ERROR))
            }
        )
    }

    private inner class StateConfigurator {
        fun defineFragmentState(): SignInViewState {
            return SignInViewState.DefaultState()
        }
    }

    class Factory @Inject constructor(
        private val stringProvider: StringProvider,
        private val interactor: Interactor,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignInViewModel(
                stringProvider,
                interactor,
            ) as T
        }
    }
}