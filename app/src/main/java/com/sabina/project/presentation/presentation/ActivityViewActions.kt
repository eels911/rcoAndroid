package com.sabina.project.presentation.presentation

sealed class ActivityViewActions {
    object Logout : ActivityViewActions()
    class Login(val userId: String) : ActivityViewActions()
    class InitLanguage(val localLanguage: String) : ActivityViewActions()
    object GetRole : ActivityViewActions()
}