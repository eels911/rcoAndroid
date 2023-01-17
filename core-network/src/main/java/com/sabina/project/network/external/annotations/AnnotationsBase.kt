package com.sabina.project.network.external.annotations

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OkHttpClientBase()

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetrofitClientBase()