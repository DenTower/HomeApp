package com.example.homeapp.presentation

import com.example.homeapp.domain.model.FamilyMember

data class HomeState(
    val members: List<FamilyMember> = emptyList(),
    val dailyAdvice: String = "Загрузка совета..."
)