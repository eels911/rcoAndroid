package com.sabina.project.local_storage.internal.di

import com.sabina.project.local_storage.external.ILocalStorageContract
import com.sabina.project.local_storage.internal.data.LocalStorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocalStorageModule {

    @Binds
    @Singleton
    abstract fun bindLocalStorageContract(impl: LocalStorageRepository): ILocalStorageContract
}