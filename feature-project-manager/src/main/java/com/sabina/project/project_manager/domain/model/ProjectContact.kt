package com.sabina.project.project_manager.domain.model

import android.os.Parcelable
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.project_manager.data.response.ProjectContactRaw
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
internal data class ProjectContact(
    var email: String = "",
    var phone: String = "",
    var name: String = "",
) : Parcelable {
    class MapperToProjectContactRaw @Inject constructor() : EssentialMapper<ProjectContact, ProjectContactRaw>() {
        override fun transform(raw: ProjectContact): ProjectContactRaw {
            return ProjectContactRaw(
                email = raw.email,
                phone = raw.phone,
                name = raw.name,
            )
        }
    }
}