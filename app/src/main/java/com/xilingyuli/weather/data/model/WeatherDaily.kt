package com.xilingyuli.weather.data.model

data class WeatherDaily(
    val date: String,
    val tempMax: Double,
    val tempMin: Double,
    val conditionDay: String,
    val conditionNight: String,
    val windDir: String,
    val windScale: String,
    val precip: Double,
    val uvIndex: Int,
)
