package com.xilingyuli.weather

import android.app.Application
import com.xilingyuli.weather.data.repository.SettingsRepository
import com.xilingyuli.weather.di.AppContainer

class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(
            wcnApiKey = BuildConfig.WCN_API_KEY,
            qwApiHost = BuildConfig.QW_API_HOST,
            qwApiKey = BuildConfig.QW_API_KEY,
            cyToken = BuildConfig.CY_TOKEN,
            settingsRepo = SettingsRepository(this),
        )
    }
}
