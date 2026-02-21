package com.example.homeapp.domain.model

import java.util.UUID

// Модель одного члена семьи
data class FamilyMember(

    // Уникальный id генерируется автоматически
    val id: String = UUID.randomUUID().toString(),

    // Имя человека
    val name: String,

    // Список его задач
    val tasks: List<Task> = emptyList()
)
