package com.megalauncher.domain.model

data class Weather(
    val city: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val description: String,
    val iconCode: String
)
