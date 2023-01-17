package com.sabina.project.base.external.mapper

object NotEmptyStringCheck : CheckerClass {
    override fun invoke(raw: Any): String {
        if (raw is String && raw.isEmpty()) {
            return "empty string"
        }
        return ""
    }
}