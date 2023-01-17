package com.sabina.project.project_manager.data

import com.sabina.project.project_manager.data.request.AddressRequest
import com.sabina.project.project_manager.data.response.SuggestionsRaw
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface AddressApi {

    @Headers(CONTENT_TYPE, ACCEPT, AUTH)
    @POST("4_1/rs/suggest/address")
    suspend fun getAddress(
        @Body request: AddressRequest
    ): SuggestionsRaw

    companion object {
        private const val CONTENT_TYPE = "Content-Type: application/json"
        private const val ACCEPT = "Accept: application/json"
        private const val AUTH = "Authorization: Token 4d187f1806837d907b60630d69740a01fe302acc"
    }
}