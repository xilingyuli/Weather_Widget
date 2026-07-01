package com.xilingyuli.weather.data.datasource

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.xilingyuli.weather.data.model.WeatherCurrent
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherLocation
import okhttp3.OkHttpClient
import okhttp3.Request

class CaiyunDataSource(
    private val token: String,
) : WeatherDataSource {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://api.caiyunapp.com/v2.6"

    override suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherCurrent> = runCatching {
        val url = "$baseUrl/$token/${location.longitude},${location.latitude}/realtime?lang=zh_CN"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, CyRealtimeResponse::class.java)
        if (data.status != "ok") throw Exception("Caiyun error: ${data.status}")
        data.toWeatherCurrent(location)
    }

    override suspend fun getDailyForecast(location: WeatherLocation, days: Int): Result<List<WeatherDaily>> = runCatching {
        val url = "$baseUrl/$token/${location.longitude},${location.latitude}/daily?lang=zh_CN&dailysteps=$days"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, CyDailyResponse::class.java)
        if (data.status != "ok") throw Exception("Caiyun error: ${data.status}")
        val resultData = data.result ?: throw Exception("No daily data")
        resultData.daily.temperature.mapIndexed { index, temp ->
            val skycon = resultData.daily.skycon.getOrNull(index)?.value?.toChinese() ?: ""
            val skyconNight = resultData.daily.skyconNightData?.getOrNull(index)?.value?.toChinese()
            val precip = resultData.daily.precipitation.getOrNull(index)
            val windData = resultData.daily.wind.getOrNull(index)
            val humidityData = resultData.daily.humidity.getOrNull(index)
            val astroData = resultData.daily.astroData?.getOrNull(index)
            WeatherDaily(
                condition = skycon,
                windDir = windDirFromDegree(windData?.avg?.direction),
                windSpeed = windData?.avg?.speed ?: 0.0,
                windScale = null,
                precip = precip?.avg ?: 0.0,
                humidity = humidityData?.avg?.let { (it * 100).toInt() },
                pressure = resultData.daily.pressure.getOrNull(index)?.avg?.let { it / 100.0 },
                visibility = resultData.daily.visibility.getOrNull(index)?.avg,
                cloudCover = resultData.daily.cloudrate.getOrNull(index)?.avg?.let { (it * 100).toInt() },
                validTime = temp.date,
                location = location,
                tempMax = temp.max,
                tempMin = temp.min,
                conditionNight = skyconNight,
                uvIndex = resultData.daily.lifeIndex?.ultraviolet?.getOrNull(index)?.index?.toIntOrNull(),
                sunrise = astroData?.sunrise?.time,
                sunset = astroData?.sunset?.time,
            )
        }
    }

    override suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherCurrent>> = runCatching {
        val url = "$baseUrl/$token/${location.longitude},${location.latitude}/hourly?lang=zh_CN&hourlysteps=$hours"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val data = gson.fromJson(body, CyHourlyResponse::class.java)
        if (data.status != "ok") throw Exception("Caiyun error: ${data.status}")
        val hourlyResult = data.result ?: throw Exception("No hourly data")
        val temps = hourlyResult.hourly.temperature
        val skycons = hourlyResult.hourly.skycon
        val precipitations = hourlyResult.hourly.precipitation
        val winds = hourlyResult.hourly.wind
        val humidities = hourlyResult.hourly.humidity
        val appTemps = hourlyResult.hourly.apparentTemperature
        val pressures = hourlyResult.hourly.pressure
        val cloudrates = hourlyResult.hourly.cloudrate
        val visibilities = hourlyResult.hourly.visibility

        temps.mapIndexed { index, temp ->
            val skycon = skycons.getOrNull(index)?.value?.toChinese() ?: ""
            val precip = precipitations.getOrNull(index)
            val wind = winds.getOrNull(index)
            val humidity = humidities.getOrNull(index)
            val appTemp = appTemps.getOrNull(index)
            val pressure = pressures.getOrNull(index)
            val cloudrate = cloudrates.getOrNull(index)
            val visibility = visibilities.getOrNull(index)

            WeatherCurrent(
                condition = skycon,
                windDir = windDirFromDegree(wind?.direction),
                windSpeed = wind?.speed ?: 0.0,
                windScale = null,
                precip = precip?.value ?: 0.0,
                humidity = humidity?.value?.let { (it * 100).toInt() },
                pressure = pressure?.value?.let { it / 100.0 },
                visibility = visibility?.value,
                cloudCover = cloudrate?.value?.let { (it * 100).toInt() },
                validTime = temp.datetime,
                location = location,
                temp = temp.value,
                feelsLike = appTemp?.value,
                pop = precip?.probability,
                dewPoint = null,
                updateTime = null,
                sourceLabel = "",
            )
        }
    }

    private fun windDirFromDegree(degree: Double?): String? {
        if (degree == null) return null
        return when {
            degree < 22.5 || degree >= 337.5 -> "北风"
            degree < 67.5 -> "东北风"
            degree < 112.5 -> "东风"
            degree < 157.5 -> "东南风"
            degree < 202.5 -> "南风"
            degree < 247.5 -> "西南风"
            degree < 292.5 -> "西风"
            degree < 337.5 -> "西北风"
            else -> null
        }
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

        fun toWeatherCurrent(location: WeatherLocation): WeatherCurrent {
            val r = result?.realtime ?: throw Exception("No realtime data")
            return WeatherCurrent(
                condition = r.skycon?.toChinese() ?: "",
                windDir = windDirFromDegree(r.wind?.direction),
                windSpeed = r.wind?.speed ?: 0.0,
                windScale = null,
                precip = r.precipitation?.local?.intensity ?: 0.0,
                humidity = (r.humidity * 100).toInt(),
                pressure = r.pressure / 100.0,
                visibility = r.visibility,
                cloudCover = (r.cloudrate * 100).toInt(),
                validTime = null,
                location = location,
                temp = r.temperature,
                feelsLike = r.apparentTemperature,
                pop = null,
                dewPoint = null,
                updateTime = null,
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
        @SerializedName("skycon_08h_20h") var skycon08h20h: List<CySkycon>? = null
        @SerializedName("skycon_20h_32h") var skycon20h32h: List<CySkycon>? = null
        @SerializedName("precipitation") var precipitation: List<CyDailyPrecip> = emptyList()
        @SerializedName("precipitation_08h_20h") var precipitation08h20h: List<CyDailyPrecip>? = null
        @SerializedName("precipitation_20h_32h") var precipitation20h32h: List<CyDailyPrecip>? = null
        @SerializedName("wind") var wind: List<CyDailyWind> = emptyList()
        @SerializedName("wind_08h_20h") var wind08h20h: List<CyDailyWind>? = null
        @SerializedName("wind_20h_32h") var wind20h32h: List<CyDailyWind>? = null
        @SerializedName("humidity") var humidity: List<CyDailyAgg> = emptyList()
        @SerializedName("cloudrate") var cloudrate: List<CyDailyAgg> = emptyList()
        @SerializedName("pressure") var pressure: List<CyDailyAgg> = emptyList()
        @SerializedName("visibility") var visibility: List<CyDailyAgg> = emptyList()
        @SerializedName("astro") var astroData: List<CyAstro>? = null
        @SerializedName("life_index") var lifeIndex: CyDailyLifeIndex? = null
        val skyconNightData: List<CySkycon>? get() = skycon20h32h
    }

    inner class CyAstro {
        @SerializedName("sunrise") var sunrise: CyAstroTime? = null
        @SerializedName("sunset") var sunset: CyAstroTime? = null
    }

    inner class CyAstroTime {
        @SerializedName("time") var time: String = ""
    }

    inner class CyDailyLifeIndex {
        @SerializedName("ultraviolet") var ultraviolet: List<CyLifeIndexItem>? = null
    }

    inner class CyLifeIndexItem {
        @SerializedName("index") var index: String = ""
        @SerializedName("desc") var desc: String = ""
    }

    inner class CyDailyWind {
        @SerializedName("date") var date: String = ""
        @SerializedName("avg") var avg: CyWindAvg? = null
    }

    inner class CyWindAvg {
        @SerializedName("speed") var speed: Double = 0.0
        @SerializedName("direction") var direction: Double = 0.0
    }

    inner class CyDailyAgg {
        @SerializedName("date") var date: String = ""
        @SerializedName("max") var max: Double = 0.0
        @SerializedName("min") var min: Double = 0.0
        @SerializedName("avg") var avg: Double = 0.0
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
        @SerializedName("temperature") var temperature: List<CyHourlyValue> = emptyList()
        @SerializedName("apparent_temperature") var apparentTemperature: List<CyHourlyValue> = emptyList()
        @SerializedName("skycon") var skycon: List<CyHourlySkycon> = emptyList()
        @SerializedName("precipitation") var precipitation: List<CyHourlyPrecip> = emptyList()
        @SerializedName("wind") var wind: List<CyHourlyWind> = emptyList()
        @SerializedName("humidity") var humidity: List<CyHourlyValue> = emptyList()
        @SerializedName("cloudrate") var cloudrate: List<CyHourlyValue> = emptyList()
        @SerializedName("pressure") var pressure: List<CyHourlyValue> = emptyList()
        @SerializedName("visibility") var visibility: List<CyHourlyValue> = emptyList()
    }

    inner class CyHourlyValue {
        @SerializedName("datetime") var datetime: String = ""
        @SerializedName("value") var value: Double = 0.0
    }

    inner class CyHourlySkycon {
        @SerializedName("datetime") var datetime: String = ""
        @SerializedName("value") var value: String = ""
    }

    inner class CyHourlyPrecip {
        @SerializedName("datetime") var datetime: String = ""
        @SerializedName("value") var value: Double = 0.0
        @SerializedName("probability") var probability: Int = 0
    }

    inner class CyHourlyWind {
        @SerializedName("datetime") var datetime: String = ""
        @SerializedName("speed") var speed: Double = 0.0
        @SerializedName("direction") var direction: Double = 0.0
    }
}
