package com.sabina.project.project_manager.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class AddressSuggestions(
    var suggestions: List<ProjectAddress> = listOf(),
) : Parcelable