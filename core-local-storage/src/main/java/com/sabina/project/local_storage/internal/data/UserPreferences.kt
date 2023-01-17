package com.sabina.project.local_storage.internal.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.sabina.project.base.external.models.SabinaRoles
import com.sabina.project.local_storage.internal.helpers.BaseRxSharedPreferences
import com.sabina.project.local_storage.internal.helpers.IPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val SP_USER_FILE_NAME = "SP_USER_FILE_NAME"

        private const val SP_COLOR_SCHEME = "SP_COLOR_SCHEME"
        private const val SP_LANGUAGE = "SP_LANGUAGE"
        private const val SP_ROLE = "SP_ROLE"
        private const val SP_USER_ID = "SP_USER_ID"
    }

    private val base = object : BaseRxSharedPreferences(context, SP_USER_FILE_NAME) {
        override fun clear() {
            role.delete()
            userId.delete()
        }
    }

    fun clear() {
        base.clear()
    }

    val colorScheme: IPreference<Int> = base.factory.getInteger(SP_COLOR_SCHEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    val language: IPreference<String> = base.factory.getString(SP_LANGUAGE, "")
    val role: IPreference<String> = base.factory.getString(SP_ROLE, SabinaRoles.CREATOR.code)
    val userId: IPreference<String> = base.factory.getString(SP_USER_ID, "")
}