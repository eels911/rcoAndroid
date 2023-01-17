package com.sabina.project.presentation.domain

import com.sabina.project.base.external.models.SabinaRoles

interface IContract {
    suspend fun getRole(userId: String): SabinaRoles
    fun signOut()
}