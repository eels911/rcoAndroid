package com.sabina.project.project_manager.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sabina.project.network.external.annotations.RetrofitClientBase
import com.sabina.project.project_manager.data.AddressApi
import com.sabina.project.project_manager.data.Repository
import com.sabina.project.project_manager.data.response.ProjectRaw
import com.sabina.project.project_manager.data.response.SuggestionsRaw
import com.sabina.project.project_manager.domain.IContract
import com.sabina.project.project_manager.domain.model.Project
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object Module {

    @Provides
    @Singleton
    fun provideRepository(
        api: FirebaseFirestore,
        storage: FirebaseStorage,
        mapperToProject: ProjectRaw.MapperToProject,
        mapperToProjectRaw: Project.MapperToProjectRaw,
        mapperToSuggestions: SuggestionsRaw.MapperToSuggestions,
        addressApi: AddressApi,
    ): IContract {
        return Repository(
            api = api,
            storage = storage,
            mapperToProject = mapperToProject,
            mapperToProjectRaw = mapperToProjectRaw,
            mapperToSuggestions = mapperToSuggestions,
            addressApi = addressApi,
        )
    }

    @Provides
    @Singleton
    fun provideAddressApi(@RetrofitClientBase retrofit: Retrofit): AddressApi {
        return retrofit.create(AddressApi::class.java)
    }
}