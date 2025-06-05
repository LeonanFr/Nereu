package com.example.nereu.network

import com.example.nereu.EnvironmentalData
import retrofit2.http.GET
import retrofit2.http.Query

interface NereuApiService {
    @GET("environmentalData")
    suspend fun getLastEnvironmentalData(): EnvironmentalData

    @GET("environmentalData/byDate")
    suspend fun getEnvironmentalDataByDate(
        @Query("date") date: String
    ): EnvironmentalDataResponse
}
