package com.sabina.project.sign_up.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sabina.project.base.external.extensions.asMap
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.base.external.models.User
import com.sabina.project.base.external.models.response.UserRaw
import com.sabina.project.sign_up.domain.IContract
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

internal class Repository @Inject constructor(
    private val api: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val mapperToUserRaw: User.MapperToUserRaw,
) : IContract {
    override suspend fun signUp(email: String, password: String): FirebaseUser? {
        return auth.createUserWithEmailAndPassword(email, password).await().user
    }

    override suspend fun addUserToList(userId: String) {
        val docRef = api.collection("Users").document(userId)
        val userRaw: UserRaw = User(role = SabinaRoles.CREATOR).essentialMap(mapperToUserRaw)
        val map = userRaw.asMap()
        docRef.set(map)
    }
}