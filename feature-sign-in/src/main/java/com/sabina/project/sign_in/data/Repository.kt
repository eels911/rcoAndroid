package com.sabina.project.sign_in.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sabina.project.sign_in.domain.IContract
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class Repository @Inject constructor(
    private val api: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : IContract {
    override suspend fun signIn(email: String, password: String): FirebaseUser? {
        return auth.signInWithEmailAndPassword(email, password).await().user
    }
}