package com.example.homeapp.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val deadline: LocalDateTime,
    val isDone: Boolean = false,
    val completedAt: LocalDateTime? = null
)