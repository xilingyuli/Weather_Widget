package com.xilingyuli.weather.data.repository

import com.xilingyuli.weather.data.datasource.DataSourceType
import com.xilingyuli.weather.data.datasource.WeatherDataSource
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherHourly
import com.xilingyuli.weather.data.model.WeatherLocation
import com.xilingyuli.weather.data.model.WeatherNow

class WeatherRepository(
    private val dataSources: Map<DataSourceType, WeatherDataSource>,
    private val settingsRepository: SettingsRepository,
    private val cache: WeatherCache,
) {
    private suspend fun getActiveDataSource(): WeatherDataSource {
        val activeType = settingsRepository.getActiveDataSourceType()
        return dataSources[activeType] ?: throw Exception("No data source found for $activeType")
    }

    suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherNow> {
        val result = getActiveDataSource().getCurrentWeather(location)
        return result.fold(
            onSuccess = { now ->
                cache.saveWeather(location.id, now)
                Result.success(now)
            },
            onFailure = { error ->
                val cached = cache.getCachedWeather(location.id)
                if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(error)
                }
            },
        )
    }

    suspend fun getDailyForecast(location: WeatherLocation, days: Int): Result<List<WeatherDaily>> {
        return getActiveDataSource().getDailyForecast(location, days)
    }

    suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherHourly>> {
        return getActiveDataSource().getHourlyForecast(location, hours)
    }
}
