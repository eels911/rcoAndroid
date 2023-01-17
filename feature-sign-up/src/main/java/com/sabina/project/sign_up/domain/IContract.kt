package com.sabina.project.sign_up.domain

import com.google.firebase.auth.FirebaseUser

internal interface IContract {
    suspend fun signUp(email: String, password: String): FirebaseUser?
    suspend fun addUserToList(userId: String)
}