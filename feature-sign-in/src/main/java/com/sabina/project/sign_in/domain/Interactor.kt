package com.sabina.project.sign_in.domain

import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

internal class Interactor @Inject constructor(
    private val repository: IContract,
) {
    suspend fun signIn(email: String, password: String): FirebaseUser? = repository.signIn(email, password)
}