package com.sabina.project.local_storage.internal.helpers

import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import kotlin.reflect.KProperty

internal interface IPreference<T> {

    val isSet: Boolean
    val key: String
    val defaultValue: T
    var value: T

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    fun delete()

    @CheckResult fun asObservable(): Observable<T>

    @CheckResult fun asConsumer(): Consumer<in T>
}