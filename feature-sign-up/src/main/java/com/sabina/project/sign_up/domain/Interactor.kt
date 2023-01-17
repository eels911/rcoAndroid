package com.sabina.project.sign_up.domain

import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

internal class Interactor @Inject constructor(
    private val repository: IContract,
) {
    suspend fun signUp(email: String, password: String): FirebaseUser? = repository.signUp(email, password)
    suspend fun addUserToList(userId: String) = repository.addUserToList(userId)
}