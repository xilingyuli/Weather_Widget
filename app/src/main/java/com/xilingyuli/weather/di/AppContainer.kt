package com.xilingyuli.weather.di

import com.xilingyuli.weather.data.datasource.CaiyunDataSource
import com.xilingyuli.weather.data.datasource.DataSourceType
import com.xilingyuli.weather.data.datasource.QWeatherDataSource
import com.xilingyuli.weather.data.datasource.WeatherCNDataSource
import com.xilingyuli.weather.data.datasource.WeatherDataSource
import com.xilingyuli.weather.data.repository.LocationRepository
import com.xilingyuli.weather.data.repository.SettingsRepository
import com.xilingyuli.weather.data.repository.WeatherCache
import com.xilingyuli.weather.data.repository.WeatherRepository

object AppContainer {

    private var weatherCNDataSource: WeatherCNDataSource? = null
    private var qWeatherDataSource: QWeatherDataSource? = null
    private var caiyunDataSource: CaiyunDataSource? = null
    private var settingsRepository: SettingsRepository? = null
    private var locationRepository: LocationRepository? = null
    private var weatherRepository: WeatherRepository? = null
    private var weatherCache: WeatherCache? = null

    fun initialize(
        wcnApiKey: String,
        qwApiHost: String,
        qwApiKey: String,
        cyToken: String,
        settingsRepo: SettingsRepository,
    ) {
        settingsRepository = settingsRepo
        weatherCache = WeatherCache(settingsRepo.context)
        locationRepository = LocationRepository(settingsRepo.context)
        weatherCNDataSource = WeatherCNDataSource(wcnApiKey)
        qWeatherDataSource = QWeatherDataSource(qwApiHost, qwApiKey)
        caiyunDataSource = CaiyunDataSource(cyToken)

        val sources: Map<DataSourceType, WeatherDataSource> = mapOf(
            DataSourceType.WEATHER_CN to weatherCNDataSource!!,
            DataSourceType.QWEATHER to qWeatherDataSource!!,
            DataSourceType.CAIYUN to caiyunDataSource!!,
        )
        weatherRepository = WeatherRepository(sources, settingsRepo, weatherCache!!, locationRepository!!)
    }

    fun getWeatherRepository(): WeatherRepository {
        return weatherRepository ?: throw IllegalStateException("AppContainer not initialized")
    }

    fun getSettingsRepository(): SettingsRepository {
        return settingsRepository ?: throw IllegalStateException("AppContainer not initialized")
    }

    fun getLocationRepository(): LocationRepository {
        return locationRepository ?: throw IllegalStateException("AppContainer not initialized")
    }
}
