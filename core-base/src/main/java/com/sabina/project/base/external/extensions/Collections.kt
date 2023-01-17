@file:Suppress("NOTHING_TO_INLINE")

package com.sabina.project.base.external.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.reactivex.functions.Function

fun <T : Any> T.asMap(): Map<*, *> {
    val json = Gson().toJson(this)
    return ObjectMapper().readValue(json, Map::class.java)
}

inline fun <T, R : Any> List<T>.mapping(transform: Function<T, R>): List<R> {
    return map { transform.apply(it!!) }
}

inline fun <T, R : Any> List<T?>.mapNotNull(transform: Function<T, R>): List<R> {
    return mapNotNull { input -> input?.let { transform.apply(it) } }
}

inline fun <T : Any> List<T?>.toOneString(): String {
    if (this.count() <= 1) return this.firstOrNull()?.toString() ?: ""
    var result = this.first().toString()
    for (i in 1 until this.count()) {
        result += ", ${this[i]}"
    }
    return result
}

inline fun <T> List<T?>?.deepAssert(): List<T> {
    return this!!.map { it!! }
}

inline fun <K, V> Map<K?, V?>?.filterNonNull(): Map<K, V> {
    if (this == null) return emptyMap()

    return entries.filter { it.value != null && it.key != null }
        .map { it.key!! to it.value!! }
        .toMap()
}

inline fun <T, R : Any> Sequence<T>.map(transform: Function<T, R>): Sequence<R> {
    return map { transform.apply(it!!) }
}

inline fun <T : Any> Sequence<T>.addIn(mutableCollection: MutableCollection<T>) {
    mutableCollection.addAll(this)
}

inline fun <T : Any> Collection<T>.addIn(mutableCollection: MutableCollection<T>) {
    mutableCollection.addAll(this)
}