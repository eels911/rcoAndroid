package com.sabina.project.sign_up.presentation

internal sealed class SignUpViewActions {
    class SignUp(
        val email: String,
        val password: String
    ) : SignUpViewActions()
}