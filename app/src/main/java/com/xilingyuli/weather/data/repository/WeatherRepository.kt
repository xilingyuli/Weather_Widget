package com.xilingyuli.weather.data.repository

import com.xilingyuli.weather.data.datasource.DataSourceType
import com.xilingyuli.weather.data.datasource.WeatherDataSource
import com.xilingyuli.weather.data.model.WeatherCurrent
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherLocation

class WeatherRepository(
    private val dataSources: Map<DataSourceType, WeatherDataSource>,
    private val settingsRepository: SettingsRepository,
    private val cache: WeatherCache,
    private val locationRepository: LocationRepository,
) {
    private suspend fun getActiveDataSource(): WeatherDataSource {
        val activeType = settingsRepository.getActiveDataSourceType()
        return dataSources[activeType] ?: throw Exception("No data source found for $activeType")
    }

    suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherCurrent> {
        val result = getActiveDataSource().getCurrentWeather(location)
        return result.fold(
            onSuccess = { current ->
                cache.saveCurrent(location.id, current)
                Result.success(current)
            },
            onFailure = { error ->
                val cached = cache.getCachedCurrent(location.id)
                if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(error)
                }
            },
        )
    }

    suspend fun getDailyForecast(location: WeatherLocation, days: Int): Result<List<WeatherDaily>> {
        val result = getActiveDataSource().getDailyForecast(location, days)
        return result.fold(
            onSuccess = { daily ->
                cache.saveDaily(location.id, daily)
                Result.success(daily)
            },
            onFailure = { error ->
                val cached = cache.getCachedDaily(location.id)
                if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(error)
                }
            },
        )
    }

    suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherCurrent>> {
        val result = getActiveDataSource().getHourlyForecast(location, hours)
        return result.fold(
            onSuccess = { hourly ->
                cache.saveHourly(location.id, hourly)
                Result.success(hourly)
            },
            onFailure = { error ->
                val cached = cache.getCachedHourly(location.id)
                if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(error)
                }
            },
        )
    }

    fun getLocations(): List<WeatherLocation> = locationRepository.getLocations()

    fun getMainLocation(): WeatherLocation? = locationRepository.getMainLocation()

    fun addLocation(location: WeatherLocation) = locationRepository.addLocation(location)

    fun removeLocation(id: String) {
        locationRepository.removeLocation(id)
        cache.clearLocation(id)
    }
}
