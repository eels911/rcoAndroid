package com.sabina.project.project_manager.data.request

internal class AddressRequest(
    val query: String,
    val language: String,
    val count: Int = 20,
)