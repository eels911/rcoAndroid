package com.sabina.project.sign_in.domain

import com.google.firebase.auth.FirebaseUser

internal interface IContract {
    suspend fun signIn(email: String, password: String): FirebaseUser?
}