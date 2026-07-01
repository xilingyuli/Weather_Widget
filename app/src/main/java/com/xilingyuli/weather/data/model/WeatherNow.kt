package com.xilingyuli.weather.data.model

data class WeatherNow(
    val temp: Double,
    val feelsLike: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val windDir: String,
    val pressure: Double,
    val precip: Double,
    val visibility: Double,
    val cloudCover: Int,
    val updateTime: String,
    val sourceLabel: String,
)
