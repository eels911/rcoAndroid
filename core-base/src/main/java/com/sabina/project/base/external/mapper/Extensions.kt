package com.sabina.project.base.external.mapper

import com.sabina.project.base.external.extensions.deepAssert
import com.sabina.project.base.external.extensions.mapping
import io.reactivex.Single

fun <T : Any, R : Any> T.essentialMap(mapper: EssentialMapper<T, R>): R = let(mapper::invoke)
fun <T : Any, R : Any> List<T?>?.essentialMap(mapper: EssentialMapper<T, R>): List<R> {
    return deepAssert().mapping(mapper)
}

fun <T : Any, R : Any> Single<T>.essentialMap(mapper: EssentialMapper<T, R>): Single<R> = map(mapper)