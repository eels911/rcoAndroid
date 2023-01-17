package com.sabina.project.presentation.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sabina.project.base.external.models.response.UserRaw
import com.sabina.project.presentation.data.Repository
import com.sabina.project.presentation.domain.IContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideRepository(
        api: FirebaseFirestore,
        auth: FirebaseAuth,
        mapperToUser: UserRaw.MapperToUser,
    ): IContract {
        return Repository(
            api = api,
            auth = auth,
            mapperToUser = mapperToUser,
        )
    }
}