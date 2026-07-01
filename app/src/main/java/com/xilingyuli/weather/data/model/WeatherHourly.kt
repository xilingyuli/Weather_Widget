package com.xilingyuli.weather.data.model

data class WeatherHourly(
    val time: String,
    val temp: Double,
    val condition: String,
    val windDir: String,
    val windScale: String,
    val precip: Double,
    val pop: Int,
)
