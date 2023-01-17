package com.sabina.project.local_storage.internal.helpers

internal interface IConverter<T> {

    fun deserialize(serialized: String): T

    fun serialize(value: T): String
}