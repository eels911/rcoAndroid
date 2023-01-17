package com.sabina.project.base.external.models

import android.os.Parcelable
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.models.response.UserRaw
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

@Parcelize
data class User(
    var role: SabinaRoles
) : Parcelable {

    class MapperToUserRaw @Inject constructor() : EssentialMapper<User, UserRaw>() {
        override fun transform(raw: User): UserRaw {
            return UserRaw(
                role = raw.role.code
            )
        }
    }
}