package com.sabina.project.base.external.models

enum class SabinaLanguage(val code: String, val mapCode: String) {
    RU("ru", "ru_RU"),
    UNKNOWN("UNKNOWN", "UNKNOWN"),
    EN("en", "en_US");

    companion object {
        fun enumValueOf(code: String): SabinaLanguage {
            return values().firstOrNull { it.code == code } ?: UNKNOWN
        }
    }
}