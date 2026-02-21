package com.example.homeapp.domain.model

import java.time.LocalDateTime
import java.util.UUID

// Модель задачи
data class Task(

    // Уникальный id задачи
    val id: String = UUID.randomUUID().toString(),

    // Название задачи
    val title: String,

    // Дата дедлайна
    val deadline: LocalDateTime,

    // Выполнена ли задача
    val isDone: Boolean = false,

    // Время выполнения (если выполнена)
    val completedAt: LocalDateTime? = null
)