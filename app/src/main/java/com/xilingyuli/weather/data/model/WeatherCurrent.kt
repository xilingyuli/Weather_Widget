package com.xilingyuli.weather.data.model

data class WeatherCurrent(
    override val condition: String,
    override val windDir: String?,
    override val windSpeed: Double,
    override val windScale: String?,
    override val precip: Double,
    override val humidity: Int?,
    override val pressure: Double?,
    override val visibility: Double?,
    override val cloudCover: Int?,
    override val validTime: String?,
    override val location: WeatherLocation,
    val temp: Double,
    val feelsLike: Double? = null,
    val pop: Int? = null,
    val dewPoint: Double? = null,
    val updateTime: String? = null,
    val sourceLabel: String = "",
) : WeatherRecord()
