package com.megalauncher.data.repository

import com.megalauncher.domain.model.Weather
import com.megalauncher.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class WeatherRepositoryImpl(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : WeatherRepository {

    override suspend fun getCurrent(city: String): Weather? = withContext(Dispatchers.IO) {
        try {
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search" +
                    "?name=${URLEncoder.encode(city, "UTF-8")}" +
                    "&count=1&language=ru&format=json"

            val geoReq = Request.Builder().url(geoUrl).build()
            val coords = client.newCall(geoReq).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val root = json.parseToJsonElement(body).jsonObject
                val results = root["results"]?.jsonArray ?: return@withContext null
                val first = results.firstOrNull()?.jsonObject ?: return@withContext null
                val lat = first["latitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
                val lon = first["longitude"]?.jsonPrimitive?.content?.toDoubleOrNull()
                val name = first["name"]?.jsonPrimitive?.content ?: city
                if (lat == null || lon == null) return@withContext null
                Triple(lat, lon, name)
            }

            val (lat, lon, resolvedName) = coords

            val weatherUrl = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code" +
                    "&timezone=auto"

            val weatherReq = Request.Builder().url(weatherUrl).build()
            client.newCall(weatherReq).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val root = json.parseToJsonElement(body).jsonObject
                val current = root["current"]?.jsonObject ?: return@withContext null

                val temp = current["temperature_2m"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    ?: return@withContext null
                val feels = current["apparent_temperature"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    ?: temp
                val humidity = current["relative_humidity_2m"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                val code = current["weather_code"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0

                Weather(
                    city = resolvedName,
                    temperature = temp,
                    feelsLike = feels,
                    humidity = humidity,
                    description = weatherCodeToText(code),
                    iconCode = code.toString()
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun weatherCodeToText(code: Int): String = when (code) {
        0 -> "Ясно"
        1, 2, 3 -> "Переменная облачность"
        45, 48 -> "Туман"
        51, 53, 55 -> "Морось"
        56, 57 -> "Ледяная морось"
        61, 63, 65 -> "Дождь"
        66, 67 -> "Ледяной дождь"
        71, 73, 75 -> "Снег"
        77 -> "Снежная крупа"
        80, 81, 82 -> "Ливень"
        85, 86 -> "Снегопад"
        95 -> "Гроза"
        96, 99 -> "Гроза с градом"
        else -> "—"
    }
}
