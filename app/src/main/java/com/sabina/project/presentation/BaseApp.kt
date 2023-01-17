package com.sabina.project.presentation

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.sabina.project.base.external.locale.LocaleUtils
import com.sabina.project.base.external.models.SabinaLanguage
import com.sabina.project.local_storage.external.ILocalStorageContract
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : Application() {
	init {
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
	}

    @Inject
    lateinit var iLocalStorageContract: ILocalStorageContract

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleUtils.updateBaseContextLocale(base!!))
    }

    // todo языки поправить snacks

	override fun onCreate() {
		super.onCreate()

        AppCompatDelegate.setDefaultNightMode(iLocalStorageContract.getColorScheme())
        initMap()
    }

    private fun initMap() {
        MapKitFactory.setApiKey("1255c8a0-3ccd-46b6-916b-e72404f36319")
        if (iLocalStorageContract.getLanguage() != SabinaLanguage.UNKNOWN)
            MapKitFactory.setLocale(iLocalStorageContract.getLanguage().mapCode) // После смены языка через настройки требуется перезапустить приложение. Сделано криво
        MapKitFactory.initialize(this)
    }
}