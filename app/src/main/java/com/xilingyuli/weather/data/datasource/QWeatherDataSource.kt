package com.xilingyuli.weather.data.datasource

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.xilingyuli.weather.data.model.QwLocation
import com.xilingyuli.weather.data.model.WeatherCurrent
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherLocation
import okhttp3.OkHttpClient
import okhttp3.Request

class QWeatherDataSource(
    private val apiHost: String,
    private val apiKey: String,
) : WeatherDataSource {

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("X-QW-Api-Key", apiKey)
                .build()
            chain.proceed(request)
        }
        .build()

    private val gson = Gson()

    override suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherCurrent> = runCatching {
        val locId = getLocationParam(location)
        val url = "https://$apiHost/v7/weather/now?location=$locId"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, QwNowResponse::class.java)
        if (data.code != "200") throw Exception("QWeather error: ${data.code}")
        data.toWeatherCurrent(location)
    }

    override suspend fun getDailyForecast(location: WeatherLocation, days: Int): Result<List<WeatherDaily>> = runCatching {
        val locId = getLocationParam(location)
        val dayEndpoint = when {
            days <= 3 -> "3d"
            days <= 7 -> "7d"
            days <= 10 -> "10d"
            else -> "7d"
        }
        val url = "https://$apiHost/v7/weather/$dayEndpoint?location=$locId"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, QwDailyResponse::class.java)
        if (data.code != "200") throw Exception("QWeather error: ${data.code}")
        data.daily.map { it.toWeatherDaily(location) }
    }

    override suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherCurrent>> = runCatching {
        val locId = getLocationParam(location)
        val hourEndpoint = when {
            hours <= 24 -> "24h"
            hours <= 72 -> "72h"
            hours <= 168 -> "168h"
            else -> "24h"
        }
        val url = "https://$apiHost/v7/weather/$hourEndpoint?location=$locId"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, QwHourlyResponse::class.java)
        if (data.code != "200") throw Exception("QWeather error: ${data.code}")
        data.hourly.map { it.toWeatherCurrent(location) }
    }

    private fun getLocationParam(location: WeatherLocation): String {
        if (location is QwLocation && location.locationId.isNotBlank()) {
            return location.locationId
        }
        return "${location.longitude},${location.latitude}"
    }

    inner class QwNowResponse {
        @SerializedName("code") var code: String = ""
        @SerializedName("now") var now: QwNowItem? = null

        fun toWeatherCurrent(location: WeatherLocation): WeatherCurrent {
            val item = now ?: throw Exception("No now data")
            return WeatherCurrent(
                condition = item.text,
                windDir = item.windDir.ifBlank { null },
                windSpeed = item.windSpeed.toDoubleOrNull() ?: 0.0,
                windScale = item.windScale.ifBlank { null },
                precip = item.precip.toDoubleOrNull() ?: 0.0,
                humidity = item.humidity.toIntOrNull(),
                pressure = item.pressure.toDoubleOrNull(),
                visibility = item.vis.toDoubleOrNull(),
                cloudCover = item.cloud.toIntOrNull(),
                validTime = item.obsTime.ifBlank { null },
                location = location,
                temp = item.temp.toDoubleOrNull() ?: 0.0,
                feelsLike = item.feelsLike.toDoubleOrNull(),
                pop = null,
                dewPoint = item.dew.toDoubleOrNull(),
                updateTime = item.obsTime.ifBlank { null },
                sourceLabel = "和风天气",
            )
        }
    }

    inner class QwNowItem {
        @SerializedName("obsTime") var obsTime: String = ""
        @SerializedName("temp") var temp: String = ""
        @SerializedName("feelsLike") var feelsLike: String = ""
        @SerializedName("icon") var icon: String = ""
        @SerializedName("text") var text: String = ""
        @SerializedName("wind360") var wind360: String = ""
        @SerializedName("windDir") var windDir: String = ""
        @SerializedName("windScale") var windScale: String = ""
        @SerializedName("windSpeed") var windSpeed: String = ""
        @SerializedName("humidity") var humidity: String = ""
        @SerializedName("precip") var precip: String = ""
        @SerializedName("pressure") var pressure: String = ""
        @SerializedName("vis") var vis: String = ""
        @SerializedName("cloud") var cloud: String = ""
        @SerializedName("dew") var dew: String = ""
    }

    inner class QwDailyResponse {
        @SerializedName("code") var code: String = ""
        @SerializedName("daily") var daily: List<QwDailyItem> = emptyList()
    }

    inner class QwDailyItem {
        @SerializedName("fxDate") var fxDate: String = ""
        @SerializedName("tempMax") var tempMax: String = ""
        @SerializedName("tempMin") var tempMin: String = ""
        @SerializedName("iconDay") var iconDay: String = ""
        @SerializedName("textDay") var textDay: String = ""
        @SerializedName("iconNight") var iconNight: String = ""
        @SerializedName("textNight") var textNight: String = ""
        @SerializedName("windDirDay") var windDirDay: String = ""
        @SerializedName("windScaleDay") var windScaleDay: String = ""
        @SerializedName("windSpeedDay") var windSpeedDay: String = ""
        @SerializedName("precip") var precip: String = ""
        @SerializedName("uvIndex") var uvIndex: String = ""
        @SerializedName("humidity") var humidity: String = ""
        @SerializedName("pressure") var pressure: String = ""
        @SerializedName("vis") var vis: String = ""
        @SerializedName("cloud") var cloud: String = ""
        @SerializedName("sunrise") var sunrise: String = ""
        @SerializedName("sunset") var sunset: String = ""

        fun toWeatherDaily(location: WeatherLocation): WeatherDaily {
            return WeatherDaily(
                condition = textDay.ifBlank { textNight },
                windDir = windDirDay.ifBlank { null },
                windSpeed = windSpeedDay.toDoubleOrNull() ?: 0.0,
                windScale = windScaleDay.ifBlank { null },
                precip = precip.toDoubleOrNull() ?: 0.0,
                humidity = humidity.toIntOrNull(),
                pressure = pressure.toDoubleOrNull(),
                visibility = vis.toDoubleOrNull(),
                cloudCover = cloud.toIntOrNull(),
                validTime = fxDate.ifBlank { null },
                location = location,
                tempMax = tempMax.toDoubleOrNull() ?: 0.0,
                tempMin = tempMin.toDoubleOrNull() ?: 0.0,
                conditionNight = textNight.ifBlank { null },
                uvIndex = uvIndex.toIntOrNull(),
                sunrise = sunrise.ifBlank { null },
                sunset = sunset.ifBlank { null },
            )
        }
    }

    inner class QwHourlyResponse {
        @SerializedName("code") var code: String = ""
        @SerializedName("hourly") var hourly: List<QwHourlyItem> = emptyList()
    }

    inner class QwHourlyItem {
        @SerializedName("fxTime") var fxTime: String = ""
        @SerializedName("temp") var temp: String = ""
        @SerializedName("icon") var icon: String = ""
        @SerializedName("text") var text: String = ""
        @SerializedName("windDir") var windDir: String = ""
        @SerializedName("windScale") var windScale: String = ""
        @SerializedName("windSpeed") var windSpeed: String = ""
        @SerializedName("precip") var precip: String = ""
        @SerializedName("pop") var pop: String = ""
        @SerializedName("humidity") var humidity: String = ""
        @SerializedName("pressure") var pressure: String = ""
        @SerializedName("cloud") var cloud: String = ""
        @SerializedName("dew") var dew: String = ""

        fun toWeatherCurrent(location: WeatherLocation): WeatherCurrent {
            return WeatherCurrent(
                condition = text,
                windDir = windDir.ifBlank { null },
                windSpeed = windSpeed.toDoubleOrNull() ?: 0.0,
                windScale = windScale.ifBlank { null },
                precip = precip.toDoubleOrNull() ?: 0.0,
                humidity = humidity.toIntOrNull(),
                pressure = pressure.toDoubleOrNull(),
                visibility = null,
                cloudCover = cloud.toIntOrNull(),
                validTime = fxTime.ifBlank { null },
                location = location,
                temp = temp.toDoubleOrNull() ?: 0.0,
                feelsLike = null,
                pop = pop.toIntOrNull(),
                dewPoint = dew.toDoubleOrNull(),
                updateTime = null,
                sourceLabel = "",
            )
        }
    }
}
