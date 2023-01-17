package com.sabina.project.sign_up.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sabina.project.base.external.models.User
import com.sabina.project.sign_up.data.Repository
import com.sabina.project.sign_up.domain.IContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object Module {

    @Provides
    @Singleton
    fun provideRepository(
        api: FirebaseFirestore,
        auth: FirebaseAuth,
        mapperToUserRaw: User.MapperToUserRaw,
    ): IContract {
        return Repository(
            api = api,
            auth = auth,
            mapperToUserRaw = mapperToUserRaw,
        )
    }
}