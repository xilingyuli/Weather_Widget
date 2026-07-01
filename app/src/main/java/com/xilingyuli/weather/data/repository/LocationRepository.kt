package com.xilingyuli.weather.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.xilingyuli.weather.data.model.QwLocation
import com.xilingyuli.weather.data.model.SimpleLocation
import com.xilingyuli.weather.data.model.WcnLocation
import com.xilingyuli.weather.data.model.WeatherLocation

class LocationRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_locations", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getLocations(): List<WeatherLocation> {
        val ids = prefs.getStringSet(KEY_LOCATION_IDS, emptySet()) ?: emptySet()
        return ids.mapNotNull { id -> getLocation(id) }
    }

    fun getLocation(id: String): WeatherLocation? {
        val json = prefs.getString(id, null) ?: return null
        return try {
            deserialize(json)
        } catch (e: Exception) {
            null
        }
    }

    fun getMainLocation(): WeatherLocation? {
        val locations = getLocations()
        return locations.lastOrNull()
    }

    fun addLocation(location: WeatherLocation) {
        val ids = (prefs.getStringSet(KEY_LOCATION_IDS, emptySet()) ?: emptySet()).toMutableSet()
        ids.add(location.id)
        prefs.edit()
            .putString(location.id, serialize(location))
            .putStringSet(KEY_LOCATION_IDS, ids)
            .apply()
    }

    fun updateLocation(location: WeatherLocation) {
        prefs.edit()
            .putString(location.id, serialize(location))
            .apply()
    }

    fun removeLocation(id: String) {
        val ids = (prefs.getStringSet(KEY_LOCATION_IDS, emptySet()) ?: emptySet()).toMutableSet()
        ids.remove(id)
        prefs.edit()
            .remove(id)
            .putStringSet(KEY_LOCATION_IDS, ids)
            .apply()
    }

    private fun serialize(location: WeatherLocation): String {
        val type = when (location) {
            is SimpleLocation -> "simple"
            is QwLocation -> "qw"
            is WcnLocation -> "wcn"
        }
        val obj = gson.toJsonTree(location).asJsonObject
        obj.addProperty("_type", type)
        return gson.toJson(obj)
    }

    private fun deserialize(json: String): WeatherLocation {
        val obj = gson.fromJson(json, JsonObject::class.java)
        val type = obj.remove("_type")?.asString ?: throw IllegalArgumentException("Missing _type")
        return when (type) {
            "simple" -> gson.fromJson(obj, SimpleLocation::class.java)
            "qw" -> gson.fromJson(obj, QwLocation::class.java)
            "wcn" -> gson.fromJson(obj, WcnLocation::class.java)
            else -> throw IllegalArgumentException("Unknown location type: $type")
        }
    }

    companion object {
        private const val KEY_LOCATION_IDS = "_location_ids"
    }
}
