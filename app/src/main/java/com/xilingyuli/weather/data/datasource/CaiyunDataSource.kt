package com.xilingyuli.weather.data.datasource

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherHourly
import com.xilingyuli.weather.data.model.WeatherLocation
import com.xilingyuli.weather.data.model.WeatherNow
import okhttp3.OkHttpClient
import okhttp3.Request

class CaiyunDataSource(
    private val token: String,
) : WeatherDataSource {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://api.caiyunapp.com/v2.6"

    override suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherNow> = runCatching {
        val url = "$baseUrl/$token/${location.longitude},${location.latitude}/realtime?lang=zh_CN"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, CyRealtimeResponse::class.java)
        if (data.status != "ok") throw Exception("Caiyun error: ${data.status}")
        data.toWeatherNow()
    }

    override suspend fun getDailyForecast(location: WeatherLocation, days: Int): Result<List<WeatherDaily>> = runCatching {
        val url = "$baseUrl/$token/${location.longitude},${location.latitude}/daily?lang=zh_CN&dailysteps=$days"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, CyDailyResponse::class.java)
        if (data.status != "ok") throw Exception("Caiyun error: ${data.status}")
        val resultData = data.result ?: throw Exception("No daily data")
        resultData.daily.temperature.mapIndexed { index, temp ->
            val skycon = resultData.daily.skycon.getOrNull(index)
            WeatherDaily(
                date = temp.date,
                tempMax = temp.max,
                tempMin = temp.min,
                conditionDay = skycon?.value?.toChinese() ?: "",
                conditionNight = "",
                windDir = "",
                windScale = "",
                precip = resultData.daily.precipitation.getOrNull(index)?.avg ?: 0.0,
                uvIndex = 0,
            )
        }
    }

    override suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherHourly>> = runCatching {
        val url = "$baseUrl/$token/${location.longitude},${location.latitude}/hourly?lang=zh_CN&hourlysteps=$hours"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, CyHourlyResponse::class.java)
        if (data.status != "ok") throw Exception("Caiyun error: ${data.status}")
        val hourlyResult = data.result ?: throw Exception("No hourly data")
        hourlyResult.hourly.temperature.map { temp ->
            WeatherHourly(
                time = temp.datetime,
                temp = temp.value,
                condition = "",
                windDir = "",
                windScale = "",
                precip = 0.0,
                pop = 0,
            )
        } ?: emptyList()
    }

    private fun String.toChinese(): String {
        return when (this) {
            "CLEAR_DAY" -> "晴"
            "CLEAR_NIGHT" -> "晴"
            "PARTLY_CLOUDY_DAY" -> "多云"
            "PARTLY_CLOUDY_NIGHT" -> "多云"
            "CLOUDY" -> "阴"
            "LIGHT_HAZE" -> "轻度霾"
            "MODERATE_HAZE" -> "中度霾"
            "HEAVY_HAZE" -> "重度霾"
            "LIGHT_RAIN" -> "小雨"
            "MODERATE_RAIN" -> "中雨"
            "HEAVY_RAIN" -> "大雨"
            "STORM_RAIN" -> "暴雨"
            "LIGHT_SNOW" -> "小雪"
            "MODERATE_SNOW" -> "中雪"
            "HEAVY_SNOW" -> "大雪"
            "STORM_SNOW" -> "暴雪"
            "LIGHT_SLEET" -> "雨夹雪"
            "DRIZZLE" -> "毛毛雨"
            "WIND" -> "大风"
            "FOG" -> "雾"
            else -> this
        }
    }

    inner class CyRealtimeResponse {
        @SerializedName("status") var status: String = ""
        @SerializedName("result") var result: CyRealtimeResult? = null

        fun toWeatherNow(): WeatherNow {
            val r = result?.realtime ?: throw Exception("No realtime data")
            return WeatherNow(
                temp = r.temperature,
                feelsLike = r.apparentTemperature,
                condition = r.skycon?.toChinese() ?: "",
                humidity = (r.humidity * 100).toInt(),
                windSpeed = r.wind?.speed ?: 0.0,
                windDir = "",
                pressure = r.pressure / 100.0,
                precip = r.precipitation?.local?.intensity ?: 0.0,
                visibility = r.visibility,
                cloudCover = (r.cloudrate * 100).toInt(),
                updateTime = "",
                sourceLabel = "彩云天气",
            )
        }
    }

    inner class CyRealtimeResult {
        @SerializedName("realtime") var realtime: CyRealtime? = null
    }

    inner class CyRealtime {
        @SerializedName("temperature") var temperature: Double = 0.0
        @SerializedName("humidity") var humidity: Double = 0.0
        @SerializedName("cloudrate") var cloudrate: Double = 0.0
        @SerializedName("skycon") var skycon: String? = null
        @SerializedName("visibility") var visibility: Double = 0.0
        @SerializedName("wind") var wind: CyWind? = null
        @SerializedName("pressure") var pressure: Double = 0.0
        @SerializedName("apparent_temperature") var apparentTemperature: Double = 0.0
        @SerializedName("precipitation") var precipitation: CyPrecipitation? = null
    }

    inner class CyWind {
        @SerializedName("speed") var speed: Double = 0.0
        @SerializedName("direction") var direction: Double = 0.0
    }

    inner class CyPrecipitation {
        @SerializedName("local") var local: CyLocalPrecip? = null
    }

    inner class CyLocalPrecip {
        @SerializedName("intensity") var intensity: Double = 0.0
        @SerializedName("datasource") var datasource: String = ""
    }

    inner class CyDailyResponse {
        @SerializedName("status") var status: String = ""
        @SerializedName("result") var result: CyDailyResult? = null
    }

    inner class CyDailyResult {
        @SerializedName("daily") var daily: CyDailyData = CyDailyData()
    }

    inner class CyDailyData {
        @SerializedName("temperature") var temperature: List<CyDailyTemp> = emptyList()
        @SerializedName("skycon") var skycon: List<CySkycon> = emptyList()
        @SerializedName("precipitation") var precipitation: List<CyDailyPrecip> = emptyList()
    }

    inner class CyDailyTemp {
        @SerializedName("date") var date: String = ""
        @SerializedName("max") var max: Double = 0.0
        @SerializedName("min") var min: Double = 0.0
        @SerializedName("avg") var avg: Double = 0.0
    }

    inner class CySkycon {
        @SerializedName("date") var date: String = ""
        @SerializedName("value") var value: String = ""
    }

    inner class CyDailyPrecip {
        @SerializedName("date") var date: String = ""
        @SerializedName("avg") var avg: Double = 0.0
        @SerializedName("max") var max: Double = 0.0
        @SerializedName("min") var min: Double = 0.0
    }

    inner class CyHourlyResponse {
        @SerializedName("status") var status: String = ""
        @SerializedName("result") var result: CyHourlyResult? = null
    }

    inner class CyHourlyResult {
        @SerializedName("hourly") var hourly: CyHourlyData = CyHourlyData()
    }

    inner class CyHourlyData {
        @SerializedName("temperature") var temperature: List<CyHourlyTemp> = emptyList()
    }

    inner class CyHourlyTemp {
        @SerializedName("datetime") var datetime: String = ""
        @SerializedName("value") var value: Double = 0.0
    }
}
