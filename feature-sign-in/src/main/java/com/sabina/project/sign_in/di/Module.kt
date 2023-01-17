package com.sabina.project.sign_in.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sabina.project.sign_in.data.Repository
import com.sabina.project.sign_in.domain.IContract
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
    ): IContract {
        return Repository(
            api = api,
            auth = auth,
        )
    }
}