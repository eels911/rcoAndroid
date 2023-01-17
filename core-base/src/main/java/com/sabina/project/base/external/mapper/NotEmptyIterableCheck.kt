package com.sabina.project.base.external.mapper

object NotEmptyIterableCheck : CheckerClass {
    override fun invoke(raw: Any): String {
        if (raw is Iterable<*> && !raw.iterator().hasNext()) {
            return "empty collection"
        }
        return ""
    }
}