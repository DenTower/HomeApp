package com.example.homeapp.domain.model

data class Advice(val text: String)

interface AdviceRepository {
    suspend fun getRandomAdvice(): Result<Advice>
}
