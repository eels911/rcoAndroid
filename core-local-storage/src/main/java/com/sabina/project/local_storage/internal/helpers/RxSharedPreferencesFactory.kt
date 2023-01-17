package com.sabina.project.local_storage.internal.helpers

import android.content.SharedPreferences
import androidx.annotation.CheckResult
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.Preference.Converter
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Observable
import io.reactivex.functions.Consumer

internal class RxSharedPreferencesFactory(sharedPreferences: SharedPreferences) : ISharedPreferencesFactory {
    private val rxSharedPreferences = RxSharedPreferences.create(sharedPreferences)

    @CheckResult
    override fun getBoolean(key: String): IPreference<Boolean> {
        return rxSharedPreferences.getBoolean(key).adaptForRxPreferences()
    }

    @CheckResult
    override fun getBoolean(key: String, defaultValue: Boolean): IPreference<Boolean> {
        return rxSharedPreferences.getBoolean(key, defaultValue).adaptForRxPreferences()
    }

    @CheckResult
    override fun <T : Enum<T>> getEnum(
        key: String,
        defaultValue: T,
        enumClass: Class<T>
    ): IPreference<T> {
        return rxSharedPreferences.getEnum(key, defaultValue, enumClass).adaptForRxPreferences()
    }

    @CheckResult
    override fun getFloat(key: String): IPreference<Float> {
        return rxSharedPreferences.getFloat(key).adaptForRxPreferences()
    }

    @CheckResult
    override fun getFloat(key: String, defaultValue: Float): IPreference<Float> {
        return rxSharedPreferences.getFloat(key, defaultValue).adaptForRxPreferences()
    }

    @CheckResult
    override fun getInteger(key: String): IPreference<Int> {
        return rxSharedPreferences.getInteger(key).adaptForRxPreferences()
    }

    @CheckResult
    override fun getInteger(key: String, defaultValue: Int): IPreference<Int> {
        return rxSharedPreferences.getInteger(key, defaultValue).adaptForRxPreferences()
    }

    @CheckResult
    override fun getLong(key: String): IPreference<Long> {
        return rxSharedPreferences.getLong(key).adaptForRxPreferences()
    }

    @CheckResult
    override fun getLong(key: String, defaultValue: Long): IPreference<Long> {
        return rxSharedPreferences.getLong(key, defaultValue).adaptForRxPreferences()
    }

    @CheckResult
    override fun <T> getObject(
        key: String,
        defaultValue: T,
        converter: IConverter<T>
    ): IPreference<T> {
        return rxSharedPreferences.getObject(key, defaultValue!!, converter.adaptForRxPreferences())
            .adaptForRxPreferences()
    }

    @CheckResult
    override fun getString(key: String): IPreference<String> {
        return rxSharedPreferences.getString(key).adaptForRxPreferences()
    }

    @CheckResult
    override fun getString(key: String, defaultValue: String): IPreference<String> {
        return rxSharedPreferences.getString(key, defaultValue).adaptForRxPreferences()
    }

    @CheckResult
    override fun getStringSet(key: String): IPreference<Set<String>> {
        return rxSharedPreferences.getStringSet(key).adaptForRxPreferences()
    }

    @CheckResult
    override fun getStringSet(
        key: String,
        defaultValue: Set<String>
    ): IPreference<Set<String>> {
        return rxSharedPreferences.getStringSet(key, defaultValue).adaptForRxPreferences()
    }

    override fun clear() {
        rxSharedPreferences.clear()
    }

    private fun <T> IConverter<T>.adaptForRxPreferences(): Converter<T> {
        val delegate = this@adaptForRxPreferences
        return object : Converter<T> {

            override fun deserialize(serialized: String): T = delegate.deserialize(serialized)

            override fun serialize(value: T): String = delegate.serialize(value)
        }
    }

    private fun <T> Preference<T>.adaptForRxPreferences(): IPreference<T> {
        val delegate = this@adaptForRxPreferences
        return object : IPreference<T> {

            override val key: String = delegate.key()

            override val defaultValue: T = delegate.defaultValue()

            override var value: T
                get() = delegate.get()
                set(value) = delegate.set(value!!)

            override val isSet: Boolean = delegate.isSet

            override fun delete() = delegate.delete()

            @CheckResult
            override fun asObservable(): Observable<T> = delegate.asObservable()

            @CheckResult
            override fun asConsumer(): Consumer<in T> = delegate.asConsumer()
        }
    }
}