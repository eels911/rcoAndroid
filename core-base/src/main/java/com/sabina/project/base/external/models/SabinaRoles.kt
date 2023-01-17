package com.sabina.project.base.external.models

enum class SabinaRoles(val code: String) {
    REVIEWER("REVIEWER"),
    CREATOR("CREATOR"),
    ADMIN("ADMIN");

    companion object {
        fun enumValueOf(code: String): SabinaRoles {
            return values().firstOrNull { it.code == code } ?: CREATOR
        }
    }
}