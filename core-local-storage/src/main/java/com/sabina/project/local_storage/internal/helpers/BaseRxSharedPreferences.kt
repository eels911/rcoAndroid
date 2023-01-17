package com.sabina.project.local_storage.internal.helpers

import android.content.Context
import android.preference.PreferenceManager

internal abstract class BaseRxSharedPreferences(
    appContext: Context,
    fileName: String? = null,
    mode: Int = Context.MODE_PRIVATE
) : ISharedPreferences {

    override val factory = RxSharedPreferencesFactory(
        if (fileName != null) {
            appContext.getSharedPreferences(fileName, mode)
        } else {
            PreferenceManager.getDefaultSharedPreferences(appContext)
        }
    )
}