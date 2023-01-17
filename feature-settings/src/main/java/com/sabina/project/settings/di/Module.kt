package com.sabina.project.settings.di

import com.sabina.project.settings.data.Repository
import com.sabina.project.settings.domain.IContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object Module {

    @Provides
    fun provideRepository(): IContract {
        return Repository()
    }
}