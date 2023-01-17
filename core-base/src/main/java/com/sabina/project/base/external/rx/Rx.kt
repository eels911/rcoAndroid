package com.sabina.project.base.external.rx

import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit

fun View.clicksWithDebounce(
    debounce: Long = 150,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    block: () -> Unit
): Disposable {
    return clicks().debounceAndObserveOnMain(debounce, timeUnit).subscribeBy(onNext = { block() })
}

fun <T : Any> Observable<T>.debounceAndObserveOnMain(
    debounce: Long = 150,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS
): Observable<T> {
    return debounce(debounce, timeUnit).observeOn(AndroidSchedulers.mainThread())
}