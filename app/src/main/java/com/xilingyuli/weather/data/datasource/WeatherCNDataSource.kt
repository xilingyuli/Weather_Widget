package com.xilingyuli.weather.data.datasource

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherHourly
import com.xilingyuli.weather.data.model.WeatherLocation
import com.xilingyuli.weather.data.model.WeatherNow
import com.xilingyuli.weather.data.model.WcnLocation
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

class WeatherCNDataSource(
    private val apiKey: String,
) : WeatherDataSource {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://openapi.weathercn.com"

    override suspend fun getCurrentWeather(location: WeatherLocation): Result<WeatherNow> = runCatching {
        val locationKey = getLocationKey(location)
        val url = "$baseUrl/currentconditions/v1/$locationKey?apikey=$apiKey&language=zh-cn&details=true"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val items = gson.fromJson(body, Array<WcnCurrentResponse>::class.java)
        val item = items.first()
        item.toWeatherNow()
    }

    override suspend fun getDailyForecast(location: WeatherLocation, days: Int): Result<List<WeatherDaily>> = runCatching {
        val locationKey = getLocationKey(location)
        val dayParam = when {
            days <= 1 -> 1
            days <= 5 -> 5
            days <= 10 -> 10
            else -> 10
        }
        val url = "$baseUrl/forecasts/v1/daily/${dayParam}day/$locationKey?apikey=$apiKey&language=zh-cn&details=true&metric=true"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val forecast = gson.fromJson(body, WcnDailyForecastResponse::class.java)
        forecast.DailyForecasts.map { it.toWeatherDaily() }
    }

    override suspend fun getHourlyForecast(location: WeatherLocation, hours: Int): Result<List<WeatherHourly>> = runCatching {
        val locationKey = getLocationKey(location)
        val hourParam = when {
            hours <= 1 -> 1
            hours <= 12 -> 12
            hours <= 24 -> 24
            hours <= 72 -> 72
            else -> 72
        }
        val url = "$baseUrl/forecasts/v1/hourly/${hourParam}hour/$locationKey?apikey=$apiKey&language=zh-cn&metric=true"
        val response = client.newCall(Request.Builder().url(url).get().build()).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")
        val items = gson.fromJson(body, Array<WcnHourlyResponse>::class.java)
        items.map { it.toWeatherHourly() }
    }

    private fun getLocationKey(location: WeatherLocation): String {
        if (location is WcnLocation) return location.locationKey
        throw IllegalArgumentException("WeatherCN requires WcnLocation")
    }

    inner class WcnCurrentResponse {
        @SerializedName("LocalObservationDateTime") var localObservationDateTime: String = ""
        @SerializedName("WeatherText") var weatherText: String = ""
        @SerializedName("WeatherIcon") var weatherIcon: Int = 0
        @SerializedName("HasPrecipitation") var hasPrecipitation: Boolean = false
        @SerializedName("PrecipitationType") var precipitationType: String? = null
        @SerializedName("IsDayTime") var isDayTime: Boolean = true
        @SerializedName("LocalSource") var localSource: WcnLocalSource? = null
        @SerializedName("Temperature") var temperature: WcnTemperature = WcnTemperature()
        @SerializedName("RealFeelTemperature") var realFeelTemperature: WcnTemperature = WcnTemperature()
        @SerializedName("RelativeHumidity") var relativeHumidity: Int = 0
        @SerializedName("Wind") var wind: WcnWind = WcnWind()
        @SerializedName("Pressure") var pressure: WcnTemperature = WcnTemperature()
        @SerializedName("Visibility") var visibility: WcnTemperature = WcnTemperature()
        @SerializedName("CloudCover") var cloudCover: Int = 0
        @SerializedName("Precip1hr") var precip1hr: WcnTemperature = WcnTemperature()
        @SerializedName("UVIndex") var uvIndex: Int = 0
        @SerializedName("MobileLink") var mobileLink: String = ""

        fun toWeatherNow(): WeatherNow {
            val sourceName = localSource?.Name ?: "Huafeng"
            val condition = localSource?.WeatherCode?.let { code ->
                wcnWeatherCodes[code] ?: weatherText
            } ?: weatherText
            return WeatherNow(
                temp = temperature.Metric?.Value ?: 0.0,
                feelsLike = realFeelTemperature.Metric?.Value ?: temperature.Metric?.Value ?: 0.0,
                condition = condition,
                humidity = relativeHumidity,
                windSpeed = wind.Speed?.Metric?.Value ?: 0.0,
                windDir = wind.Direction?.Localized ?: "",
                pressure = pressure.Metric?.Value ?: 0.0,
                precip = precip1hr.Metric?.Value ?: 0.0,
                visibility = visibility.Metric?.Value ?: 0.0,
                cloudCover = cloudCover,
                updateTime = localObservationDateTime,
                sourceLabel = "华风天气",
            )
        }
    }

    inner class WcnLocalSource {
        @SerializedName("Id") var Id: Int = 0
        @SerializedName("Name") var Name: String = ""
        @SerializedName("WeatherCode") var WeatherCode: String = ""
        @SerializedName("WindLevel") var WindLevel: String = ""
    }

    inner class WcnTemperature {
        @SerializedName("Metric") var Metric: WcnMetricValue? = null
        @SerializedName("Imperial") var Imperial: Any? = null
    }

    inner class WcnMetricValue {
        @SerializedName("Value") var Value: Double = 0.0
        @SerializedName("Unit") var Unit: String = ""
    }

    inner class WcnWind {
        @SerializedName("Direction") var Direction: WcnWindDirection? = null
        @SerializedName("Speed") var Speed: WcnWindSpeed? = null
    }

    inner class WcnWindDirection {
        @SerializedName("Degrees") var Degrees: Int = 0
        @SerializedName("Localized") var Localized: String = ""
    }

    inner class WcnWindSpeed {
        @SerializedName("Metric") var Metric: WcnMetricValue? = null
    }

    inner class WcnDailyForecastResponse {
        @SerializedName("DailyForecasts") var DailyForecasts: List<WcnDailyItem> = emptyList()
    }

    inner class WcnDailyItem {
        @SerializedName("Date") var Date: String = ""
        @SerializedName("Temperature") var Temperature: WcnDailyTemp = WcnDailyTemp()
        @SerializedName("Day") var Day: WcnDayNight = WcnDayNight()
        @SerializedName("Night") var Night: WcnDayNight = WcnDayNight()
        @SerializedName("AirAndPollen") var AirAndPollen: List<WcnPollen> = emptyList()

        fun toWeatherDaily(): WeatherDaily {
            return WeatherDaily(
                date = Date.take(10),
                tempMax = Temperature.Maximum?.Value ?: 0.0,
                tempMin = Temperature.Minimum?.Value ?: 0.0,
                conditionDay = Day.IconPhrase,
                conditionNight = Night.IconPhrase,
                windDir = Day.Wind?.Direction?.Localized ?: "",
                windScale = Day.Wind?.Speed?.Metric?.Value?.toString() ?: "",
                precip = Day.TotalLiquid?.Value ?: 0.0,
                uvIndex = 0,
            )
        }
    }

    inner class WcnDailyTemp {
        @SerializedName("Minimum") var Minimum: WcnMetricValue? = null
        @SerializedName("Maximum") var Maximum: WcnMetricValue? = null
    }

    inner class WcnDayNight {
        @SerializedName("Icon") var Icon: Int = 0
        @SerializedName("IconPhrase") var IconPhrase: String = ""
        @SerializedName("TotalLiquid") var TotalLiquid: WcnMetricValue? = null
        @SerializedName("Wind") var Wind: WcnWind? = null
    }

    inner class WcnPollen {
        @SerializedName("Name") var Name: String = ""
        @SerializedName("Value") var Value: Int = 0
        @SerializedName("Category") var Category: String = ""
    }

    inner class WcnHourlyResponse {
        @SerializedName("DateTime") var DateTime: String = ""
        @SerializedName("EpochDateTime") var EpochDateTime: Long = 0
        @SerializedName("WeatherIcon") var WeatherIcon: Int = 0
        @SerializedName("IconPhrase") var IconPhrase: String = ""
        @SerializedName("HasPrecipitation") var HasPrecipitation: Boolean = false
        @SerializedName("IsDaylight") var IsDaylight: Boolean = true
        @SerializedName("Temperature") var Temperature: WcnMetricValue? = null
        @SerializedName("Wind") var Wind: WcnWind? = null

        fun toWeatherHourly(): WeatherHourly {
            return WeatherHourly(
                time = DateTime,
                temp = Temperature?.Value ?: 0.0,
                condition = IconPhrase,
                windDir = Wind?.Direction?.Localized ?: "",
                windScale = Wind?.Speed?.Metric?.Value?.toString() ?: "",
                precip = 0.0,
                pop = 0,
            )
        }
    }

    companion object {
        val wcnWeatherCodes = mapOf(
            "00" to "晴", "01" to "多云", "02" to "阴", "03" to "阵雨",
            "04" to "雷阵雨", "05" to "雷阵雨伴有冰雹", "06" to "雨夹雪",
            "07" to "小雨", "08" to "中雨", "09" to "大雨", "10" to "暴雨",
            "11" to "大暴雨", "12" to "特大暴雨", "13" to "阵雪", "14" to "小雪",
            "15" to "中雪", "16" to "大雪", "17" to "暴雪", "18" to "雾",
            "19" to "冻雨", "20" to "沙尘暴", "21" to "小到中雨", "22" to "中到大雨",
            "23" to "大到暴雨", "24" to "暴雨到大暴雨", "25" to "大暴雨到特大暴雨",
            "26" to "小到中雪", "27" to "中到大雪", "28" to "大到暴雪",
            "29" to "浮尘", "30" to "扬沙", "31" to "强沙尘暴", "32" to "浓雾",
            "33" to "雪", "49" to "强浓雾", "53" to "霾", "54" to "中度霾",
            "55" to "重度霾", "56" to "严重霾", "57" to "大雾", "58" to "特强浓雾",
            "301" to "降雨", "302" to "降雪",
        )
    }
}
