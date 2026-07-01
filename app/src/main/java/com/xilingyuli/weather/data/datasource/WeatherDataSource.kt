package com.xilingyuli.weather.data.datasource

import com.xilingyuli.weather.data.model.WeatherCurrent
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherLocation

interface WeatherDataSource {
    suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherCurrent>

    suspend fun getDailyForecast(location: WeatherLocation, days: Int): Result<List<WeatherDaily>>

    suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherCurrent>>
}
