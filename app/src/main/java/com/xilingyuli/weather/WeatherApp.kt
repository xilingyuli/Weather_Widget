package com.xilingyuli.weather

import android.app.Application
import com.xilingyuli.weather.data.repository.SettingsRepository
import com.xilingyuli.weather.di.AppContainer
import com.xilingyuli.weather.ui.Defaults

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

        val repo = AppContainer.getLocationRepository()
        if (repo.getLocations().isEmpty()) {
            repo.addLocation(Defaults.BEIJING_HAIDIAN)
            repo.addLocation(Defaults.PINGDINGSHAN_WEIDONG)
            repo.addLocation(
                com.xilingyuli.weather.data.model.SimpleLocation(
                    id = Defaults.LOCATION_AUTO_ID,
                    name = "自动定位",
                    province = "",
                    city = "等待定位",
                    district = null,
                    country = "",
                    latitude = 0.0,
                    longitude = 0.0,
                )
            )
        }
    }
}
