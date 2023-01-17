package com.sabina.project.sign_in.presentation

internal sealed class SignInViewActions {
    class SignIn(
        val email: String,
        val password: String
    ) : SignInViewActions()
}