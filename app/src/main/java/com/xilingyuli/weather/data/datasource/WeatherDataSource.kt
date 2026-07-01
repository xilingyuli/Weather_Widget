package com.xilingyuli.weather.data.datasource

import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherHourly
import com.xilingyuli.weather.data.model.WeatherLocation
import com.xilingyuli.weather.data.model.WeatherNow

interface WeatherDataSource {
    suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherNow>

    suspend fun getDailyForecast(location: WeatherLocation, days: Int): Result<List<WeatherDaily>>

    suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherHourly>>
}
