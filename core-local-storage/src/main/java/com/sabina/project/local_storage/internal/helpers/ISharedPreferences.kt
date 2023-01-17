package com.sabina.project.local_storage.internal.helpers

internal interface ISharedPreferences : IStore {
    val factory: ISharedPreferencesFactory
}