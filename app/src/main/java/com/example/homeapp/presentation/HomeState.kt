package com.example.homeapp.presentation

import com.example.homeapp.domain.model.FamilyMember

// Главный UI-стейт экрана "Дом"
// ViewModel будет отдавать именно этот объект в UI
data class HomeState(
    // Список членов семьи (каждый со своими задачами)
    val members: List<FamilyMember> = emptyList(),

    // Текст случайного совета дня
    val dailyAdvice: String = "Загрузка совета..."
)