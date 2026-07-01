package com.xilingyuli.weather.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import com.xilingyuli.weather.data.model.WeatherLocation

class LocationProvider(context: Context) {

    @SuppressLint("MissingPermission")
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun getNetworkLocation(): WeatherLocation? {
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: return null
        return WeatherLocation(
            id = "auto_${System.currentTimeMillis()}",
            name = "${location.latitude},${location.longitude}",
            province = "",
            city = "",
            district = null,
            country = "",
            latitude = location.latitude,
            longitude = location.longitude,
        )
    }

    fun isProviderEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    data class WeatherLocation(
        val id: String,
        val name: String,
        val province: String,
        val city: String,
        val district: String?,
        val country: String,
        val latitude: Double,
        val longitude: Double,
    )
}
