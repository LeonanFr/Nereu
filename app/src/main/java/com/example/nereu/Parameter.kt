package com.example.nereu

import kotlinx.serialization.Serializable

@Serializable
data class EnvironmentalData(
    val timestamp: String,
    val temperature: Float? = null,
    val salinity: Float? = null,
    val pressure: Float? = null
) {
    fun getValueForParameter(param: String): Float? {
        return when (param) {
            "temperature" -> this.temperature
            "salinity" -> this.salinity
            "pressure" -> this.pressure
            else -> null
        }
    }
}
