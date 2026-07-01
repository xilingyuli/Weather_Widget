package com.xilingyuli.weather.data.model

sealed class WeatherRecord {
    abstract val condition: String
    abstract val windDir: String?
    abstract val windSpeed: Double
    abstract val windScale: String?
    abstract val precip: Double
    abstract val humidity: Int?
    abstract val pressure: Double?
    abstract val visibility: Double?
    abstract val cloudCover: Int?
    abstract val validTime: String?
    abstract val location: WeatherLocation
}
