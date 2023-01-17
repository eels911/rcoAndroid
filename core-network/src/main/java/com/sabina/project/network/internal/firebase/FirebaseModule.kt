package com.sabina.project.network.internal.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object FirebaseModule {

    @Provides
    @Singleton
	fun provideFirebaseInstance(): FirebaseFirestore {
		return FirebaseFirestore.getInstance()
	}

    @Provides
    @Singleton
	fun provideFirebaseStorage(): FirebaseStorage {
		return FirebaseStorage.getInstance()
	}

    @Provides
    @Singleton
	fun provideFirebaseAuth(): FirebaseAuth {
		return FirebaseAuth.getInstance()
	}
}