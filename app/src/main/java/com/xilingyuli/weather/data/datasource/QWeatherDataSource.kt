package com.xilingyuli.weather.data.datasource

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.xilingyuli.weather.data.model.QwLocation
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherHourly
import com.xilingyuli.weather.data.model.WeatherLocation
import com.xilingyuli.weather.data.model.WeatherNow
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

    override suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherNow> = runCatching {
        val locId = getLocationParam(location)
        val url = "https://$apiHost/v7/weather/now?location=$locId"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, QwNowResponse::class.java)
        if (data.code != "200") throw Exception("QWeather error: ${data.code}")
        data.toWeatherNow()
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
        data.daily.map { it.toWeatherDaily() }
    }

    override suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherHourly>> = runCatching {
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
        data.hourly.map { it.toWeatherHourly() }
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

        fun toWeatherNow(): WeatherNow {
            val item = now ?: throw Exception("No now data")
            return WeatherNow(
                temp = item.temp.toDoubleOrNull() ?: 0.0,
                feelsLike = item.feelsLike.toDoubleOrNull() ?: 0.0,
                condition = item.text,
                humidity = item.humidity.toIntOrNull() ?: 0,
                windSpeed = item.windSpeed.toDoubleOrNull() ?: 0.0,
                windDir = item.windDir,
                pressure = item.pressure.toDoubleOrNull() ?: 0.0,
                precip = item.precip.toDoubleOrNull() ?: 0.0,
                visibility = item.vis.toDoubleOrNull() ?: 0.0,
                cloudCover = item.cloud.toIntOrNull() ?: 0,
                updateTime = item.obsTime,
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
        @SerializedName("precip") var precip: String = ""
        @SerializedName("uvIndex") var uvIndex: String = ""

        fun toWeatherDaily(): WeatherDaily {
            return WeatherDaily(
                date = fxDate,
                tempMax = tempMax.toDoubleOrNull() ?: 0.0,
                tempMin = tempMin.toDoubleOrNull() ?: 0.0,
                conditionDay = textDay,
                conditionNight = textNight,
                windDir = windDirDay,
                windScale = windScaleDay,
                precip = precip.toDoubleOrNull() ?: 0.0,
                uvIndex = uvIndex.toIntOrNull() ?: 0,
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
        @SerializedName("precip") var precip: String = ""
        @SerializedName("pop") var pop: String = ""

        fun toWeatherHourly(): WeatherHourly {
            return WeatherHourly(
                time = fxTime,
                temp = temp.toDoubleOrNull() ?: 0.0,
                condition = text,
                windDir = windDir,
                windScale = windScale,
                precip = precip.toDoubleOrNull() ?: 0.0,
                pop = pop.toIntOrNull() ?: 0,
            )
        }
    }
}
