package com.sabina.project.base.external.mapper

object NotEmptyStringIterableCheck : CheckerClass {
    override fun invoke(raw: Any): String {
        if (raw is Iterable<*>) {
            if (!raw.iterator().hasNext()) {
                return "empty iterable"
            } else if (raw::class.typeParameters[0] == String::class &&
                raw.all { (it as String).isEmpty() }) {
                return "iterable with only empty strings"
            }
        }
        return ""
    }
}