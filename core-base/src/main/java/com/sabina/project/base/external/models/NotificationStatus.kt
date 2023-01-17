package com.sabina.project.base.external.models

enum class NotificationStatus(val index: Int) {
    SUCCESS(2),
    WARNING(1),
    ERROR(0);

    companion object {
        fun getByIndex(index: Int): NotificationStatus {
            return when (index) {
                2 -> SUCCESS
                1 -> WARNING
                else -> ERROR
            }
        }
    }
}