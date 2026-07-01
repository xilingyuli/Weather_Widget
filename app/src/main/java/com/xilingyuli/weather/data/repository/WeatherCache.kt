package com.xilingyuli.weather.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xilingyuli.weather.data.model.WeatherCurrent
import com.xilingyuli.weather.data.model.WeatherDaily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherCache(context: Context) {

    private val gson = Gson()
    private val appContext = context.applicationContext
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
    private val hourFormat = SimpleDateFormat("yyyyMMdd_HH", Locale.US)

    private fun getPrefs(locationId: String): SharedPreferences {
        return appContext.getSharedPreferences("weather_loc_$locationId", Context.MODE_PRIVATE)
    }

    fun getCachedCurrent(locationId: String): WeatherCurrent? {
        val prefs = getPrefs(locationId)
        if (prefs.all.isEmpty()) return null

        val cal = Calendar.getInstance()
        val today = dateFormat.format(cal.time)
        val currentHour = hourFormat.format(cal.time)

        // 1. Exact now_ key match
        prefs.getString("${PREFIX_NOW}$currentHour", null)?.let {
            return safeParse(it, WeatherCurrent::class.java)
        }

        // 2. Search backwards: h_{hour}, now_{hour-1}, h_{hour-1}, ... up to 3h
        val hourlyMap = buildHourlyMap(prefs)
        var crossedYesterday = false
        for (offset in 0..3) {
            if (offset > 0) {
                cal.time = Date()
                cal.add(Calendar.HOUR_OF_DAY, -offset)
                if (dateFormat.format(cal.time) != today) crossedYesterday = true
            }
            val targetKey = hourFormat.format(cal.time)
            if (offset > 0) {
                prefs.getString("${PREFIX_NOW}$targetKey", null)?.let {
                    return safeParse(it, WeatherCurrent::class.java)
                }
            }
            hourlyMap[targetKey]?.let { return it }
        }

        // 3. 3h exhausted and crossed into yesterday → try today's daily
        if (crossedYesterday) {
            val dailyList = buildDailyList(prefs)
            val todayDaily = dailyList.find { it.validTime?.take(10) == today }
            if (todayDaily != null) {
                return dailyToCurrent(todayDaily, "降级-来自日预报")
            }
        }

        return null
    }

    fun saveCurrent(locationId: String, weather: WeatherCurrent) {
        val key = "${PREFIX_NOW}${hourFormat.format(Date())}"
        getPrefs(locationId).edit()
            .putString(key, gson.toJson(weather))
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }

    fun getCachedDaily(locationId: String): List<WeatherDaily>? {
        return buildDailyList(getPrefs(locationId)).ifEmpty { null }
    }

    fun saveDaily(locationId: String, daily: List<WeatherDaily>) {
        val key = "${PREFIX_DAILY}${dateFormat.format(Date())}"
        getPrefs(locationId).edit()
            .putString(key, gson.toJson(daily))
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }

    fun getCachedHourly(locationId: String): List<WeatherCurrent>? {
        val map = buildHourlyMap(getPrefs(locationId))
        return map.values.toList().ifEmpty { null }
    }

    fun saveHourly(locationId: String, hourly: List<WeatherCurrent>) {
        val key = "${PREFIX_HOURLY}${hourFormat.format(Date())}"
        getPrefs(locationId).edit()
            .putString(key, gson.toJson(hourly))
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }

    fun getLastUpdateTime(locationId: String): Long {
        return getPrefs(locationId).getLong(KEY_LAST_UPDATE, 0)
    }

    fun clearLocation(locationId: String) {
        getPrefs(locationId).edit().clear().apply()
    }

    private fun buildHourlyMap(prefs: SharedPreferences): Map<String, WeatherCurrent> {
        val entries = prefs.all.filterKeys { it.startsWith(PREFIX_HOURLY) }
        if (entries.isEmpty()) return emptyMap()
        val map = linkedMapOf<String, WeatherCurrent>()
        val type = object : TypeToken<List<WeatherCurrent>>() {}.type
        for ((_, value) in entries) {
            try {
                val list = gson.fromJson<List<WeatherCurrent>>(value as String, type)
                for (item in list) {
                    val hourKey = item.validTime?.take(13)
                    if (hourKey != null && hourKey !in map) {
                        map[hourKey] = item
                    }
                }
            } catch (_: Exception) {
            }
        }
        return map
    }

    private fun buildDailyList(prefs: SharedPreferences): List<WeatherDaily> {
        val entries = prefs.all.filterKeys { it.startsWith(PREFIX_DAILY) }
        if (entries.isEmpty()) return emptyList()
        val all = mutableListOf<WeatherDaily>()
        val type = object : TypeToken<List<WeatherDaily>>() {}.type
        for ((_, value) in entries) {
            try {
                all.addAll(gson.fromJson(value as String, type))
            } catch (_: Exception) {
            }
        }
        return all.distinctBy { it.validTime }
    }

    private fun dailyToCurrent(daily: WeatherDaily, sourceLabel: String): WeatherCurrent {
        return WeatherCurrent(
            condition = daily.condition,
            windDir = daily.windDir,
            windSpeed = daily.windSpeed,
            windScale = daily.windScale,
            precip = daily.precip,
            humidity = daily.humidity,
            pressure = daily.pressure,
            visibility = daily.visibility,
            cloudCover = daily.cloudCover,
            validTime = daily.validTime,
            location = daily.location,
            temp = (daily.tempMax + daily.tempMin) / 2.0,
            feelsLike = null,
            pop = null,
            dewPoint = null,
            updateTime = null,
            sourceLabel = sourceLabel,
        )
    }

    private inline fun <reified T> safeParse(json: String, clazz: Class<T>): T? {
        return try {
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PREFIX_NOW = "now_"
        private const val KEY_LAST_UPDATE = "last_update"
        private const val PREFIX_DAILY = "d_"
        private const val PREFIX_HOURLY = "h_"
    }
}
