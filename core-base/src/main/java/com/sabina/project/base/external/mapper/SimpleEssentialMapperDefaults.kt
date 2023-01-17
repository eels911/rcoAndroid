package com.sabina.project.base.external.mapper

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

fun getDefault(property: KProperty<*>): Any {
    return when (property.returnType.classifier as KClass<*>) {
        String::class -> ""
        Int::class -> 0
        Double::class -> 0.0
        Long::class -> 0L
        List::class -> emptyList<Any>()
        Set::class -> emptySet<Any>()
        else -> throw UnsupportDefaultTypeException(property.returnType.toString())
    }
}