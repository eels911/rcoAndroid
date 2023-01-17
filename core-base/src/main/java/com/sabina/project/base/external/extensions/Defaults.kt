@file:Suppress("NOTHING_TO_INLINE")

package com.sabina.project.base.external.extensions

import java.util.Date

inline fun Byte?.orDefault(): Byte = this ?: 0

inline fun Short?.orDefault(): Short = this ?: 0

inline fun Int?.orDefault(): Int = this ?: 0

inline fun Long?.orDefault(): Long = this ?: 0

inline fun Float?.orDefault(): Float = this ?: 0f

inline fun Double?.orDefault(): Double = this ?: 0.0

inline fun String?.orDefault(): String = this ?: ""

inline fun Date?.orDefault(): Date = this ?: Date(0)

inline fun Boolean?.orDefault(): Boolean = this ?: false