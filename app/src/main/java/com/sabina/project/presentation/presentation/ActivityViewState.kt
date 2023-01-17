package com.sabina.project.presentation.presentation

import com.sabina.project.base.external.viewState.ViewState

sealed class ActivityViewState : ViewState() {
    object Auth : ActivityViewState()
    object LoggedIn : ActivityViewState()
}