package com.sabina.project.sign_up.presentation

import androidx.constraintlayout.motion.utils.ViewState

internal sealed class SignUpViewState : ViewState() {

    class DefaultState() : SignUpViewState()
}