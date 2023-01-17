package com.sabina.project.presentation.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.sabina.project.base.external.mapper.essentialMap
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.base.external.models.response.UserRaw
import com.sabina.project.presentation.domain.IContract
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class Repository @Inject constructor(
    private val api: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val mapperToUser: UserRaw.MapperToUser,
) : IContract {
    override suspend fun getRole(userId: String): SabinaRoles {
        val docRef = api.collection("Users").document(userId)
        val document = docRef.get().await()
        val json = Gson().toJson(document.data?.toMap())
        val userRaw: UserRaw = Gson().fromJson(json, UserRaw::class.java)
        val user = userRaw.essentialMap(mapperToUser)
        return SabinaRoles.enumValueOf(user.role.code)
    }

    override fun signOut() {
        auth.signOut()
    }
}