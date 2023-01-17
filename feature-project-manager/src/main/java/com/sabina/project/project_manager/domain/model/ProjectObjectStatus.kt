package com.sabina.project.project_manager.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal enum class ProjectObjectStatus(val type: String) : Parcelable {
    CHECKED("CHECKED"),
    INCOMPLETE("INCOMPLETE"),
    READY_FOR_REVIEW("READY_FOR_REVIEW"),
    CREATED("CREATED");

    companion object {
        fun byName(type: String): ProjectObjectStatus {
            return values().first { it.type == type }
        }
    }
}