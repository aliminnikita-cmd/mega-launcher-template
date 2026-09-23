package com.megalauncher.domain.repository

import com.megalauncher.domain.model.Weather

interface WeatherRepository {
    suspend fun getCurrent(city: String): Weather?
}
