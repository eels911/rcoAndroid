package com.sabina.project.base.external.extensions

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.PluralsRes
import androidx.core.content.ContextCompat
import kotlin.math.ceil

fun Context.pxToDp(px: Int) = px / this.resources.displayMetrics.density

fun Context.dpToPx(value: Number): Int {
	return if (value != 0) {
		ceil(resources.displayMetrics.density * value.toDouble()).toInt()
	} else {
		0
	}
}

fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
	return ContextCompat.getColor(this, colorRes)
}

fun Context.getPlurals(@PluralsRes id: Int, count: Int, vararg formatArgs: Any): String {
    return resources.getQuantityString(id, count, *formatArgs)
}