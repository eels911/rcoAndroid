package com.sabina.project.sign_in.presentation

import androidx.constraintlayout.motion.utils.ViewState

internal sealed class SignInViewState : ViewState() {

    class DefaultState() : SignInViewState()
}