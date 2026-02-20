package com.example.homeapp.data.remote

import retrofit2.http.GET

interface AdviceApi {
    @GET("advice")
    suspend fun fetchRandomAdvice(): AdviceDto
}

// DTO для парсинга JSON
data class AdviceDto(val slip: SlipDto)
data class SlipDto(val id: Int, val advice: String)