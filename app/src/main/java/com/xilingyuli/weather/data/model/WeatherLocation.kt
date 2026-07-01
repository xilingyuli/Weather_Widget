package com.xilingyuli.weather.data.model

sealed class WeatherLocation {
    abstract val id: String
    abstract val name: String
    abstract val province: String
    abstract val city: String
    abstract val district: String?
    abstract val country: String
    abstract val latitude: Double
    abstract val longitude: Double
}

data class SimpleLocation(
    override val id: String,
    override val name: String,
    override val province: String,
    override val city: String,
    override val district: String?,
    override val country: String,
    override val latitude: Double,
    override val longitude: Double,
) : WeatherLocation()

data class QwLocation(
    override val id: String,
    override val name: String,
    override val province: String,
    override val city: String,
    override val district: String?,
    override val country: String,
    override val latitude: Double,
    override val longitude: Double,
    val locationId: String,
) : WeatherLocation()

data class WcnLocation(
    override val id: String,
    override val name: String,
    override val province: String,
    override val city: String,
    override val district: String?,
    override val country: String,
    override val latitude: Double,
    override val longitude: Double,
    val locationKey: String,
) : WeatherLocation()
