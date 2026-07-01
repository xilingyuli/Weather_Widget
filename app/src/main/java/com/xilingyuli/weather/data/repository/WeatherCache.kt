package com.xilingyuli.weather.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.xilingyuli.weather.data.model.WeatherNow

class WeatherCache(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getCachedWeather(locationId: String): WeatherNow? {
        val json = prefs.getString("now_$locationId", null) ?: return null
        return try {
            gson.fromJson(json, WeatherNow::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun saveWeather(locationId: String, weather: WeatherNow) {
        prefs.edit()
            .putString("now_$locationId", gson.toJson(weather))
            .putLong("last_update_$locationId", System.currentTimeMillis())
            .apply()
    }

    fun getLastUpdateTime(locationId: String): Long {
        return prefs.getLong("last_update_$locationId", 0)
    }
}
