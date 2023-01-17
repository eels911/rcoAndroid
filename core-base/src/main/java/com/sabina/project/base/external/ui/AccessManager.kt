package com.sabina.project.base.external.ui

interface AccessManager {
	fun logout()
	fun login(userId: String)
}