package com.sabina.project.local_storage.internal.helpers

import android.os.Build.VERSION_CODES.HONEYCOMB
import androidx.annotation.CheckResult
import androidx.annotation.RequiresApi

internal interface ISharedPreferencesFactory {

    @CheckResult fun getBoolean(key: String): IPreference<Boolean>

    @CheckResult fun getBoolean(key: String, defaultValue: Boolean): IPreference<Boolean>

    @CheckResult fun <T : Enum<T>> getEnum(
        key: String,
        defaultValue: T,
        enumClass: Class<T>
    ): IPreference<T>

    @CheckResult fun getFloat(key: String): IPreference<Float>

    @CheckResult fun getFloat(key: String, defaultValue: Float): IPreference<Float>

    @CheckResult fun getInteger(key: String): IPreference<Int>

    @CheckResult fun getInteger(key: String, defaultValue: Int): IPreference<Int>

    @CheckResult fun getLong(key: String): IPreference<Long>

    @CheckResult fun getLong(key: String, defaultValue: Long): IPreference<Long>

    @CheckResult fun <T> getObject(
        key: String,
        defaultValue: T,
        converter: IConverter<T>
    ): IPreference<T>

    @CheckResult fun getString(key: String): IPreference<String>

    @CheckResult fun getString(key: String, defaultValue: String): IPreference<String>

    @RequiresApi(HONEYCOMB)
    @CheckResult
    fun getStringSet(key: String): IPreference<Set<String>>

    @RequiresApi(HONEYCOMB)
    @CheckResult
    fun getStringSet(key: String, defaultValue: Set<String>): IPreference<Set<String>>

    fun clear()
}