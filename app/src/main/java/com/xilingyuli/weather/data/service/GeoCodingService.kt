package com.xilingyuli.weather.data.service

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.xilingyuli.weather.data.model.QwLocation
import okhttp3.OkHttpClient
import okhttp3.Request

class GeoCodingService(private val apiKey: String) {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://geoapi.qweather.com/v2/city"

    data class GeoResult(
        val location: QwLocation?,
        val error: String?,
    )

    fun reverseLookup(latitude: Double, longitude: Double): GeoResult {
        return try {
            val url = "$baseUrl/lookup?location=$longitude,$latitude&key=$apiKey"
            val response = client.newCall(Request.Builder().url(url).get().build()).execute()
            val body = response.body?.string() ?: return GeoResult(null, "Empty response")
            val data = gson.fromJson(body, QwGeoResponse::class.java)
            if (data.code != "200") return GeoResult(null, "Geo error: ${data.code}")
            val top = data.locationList.firstOrNull() ?: return GeoResult(null, "No location found")
            GeoResult(
                QwLocation(
                    id = "${top.adm1}_${top.name}_${top.id}",
                    name = top.name,
                    province = top.adm1,
                    city = top.adm2,
                    district = top.name,
                    country = top.country,
                    latitude = top.lat.toDoubleOrNull() ?: latitude,
                    longitude = top.lon.toDoubleOrNull() ?: longitude,
                    locationId = top.id,
                ),
                null,
            )
        } catch (e: Exception) {
            GeoResult(null, e.message ?: "Unknown error")
        }
    }

    inner class QwGeoResponse {
        @SerializedName("code") var code: String = ""
        @SerializedName("location") var locationList: List<QwGeoLocation> = emptyList()
    }

    inner class QwGeoLocation {
        @SerializedName("name") var name: String = ""
        @SerializedName("id") var id: String = ""
        @SerializedName("lat") var lat: String = ""
        @SerializedName("lon") var lon: String = ""
        @SerializedName("adm1") var adm1: String = ""
        @SerializedName("adm2") var adm2: String = ""
        @SerializedName("country") var country: String = ""
    }
}
