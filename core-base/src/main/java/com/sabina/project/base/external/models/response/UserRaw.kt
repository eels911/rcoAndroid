package com.sabina.project.base.external.models.response

import com.google.gson.annotations.SerializedName
import com.sabina.project.base.external.mapper.EssentialMapper
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.base.external.models.User
import javax.inject.Inject

data class UserRaw(
    @SerializedName("role") val role: String = "",
) {
    class MapperToUser @Inject constructor() : EssentialMapper<UserRaw, User>() {
        override fun transform(raw: UserRaw): User {
            return User(
                role = SabinaRoles.enumValueOf(raw.role)
            )
        }
    }
}