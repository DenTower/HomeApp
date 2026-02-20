package com.example.homeapp.domain.model

import java.util.UUID

data class FamilyMember(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val tasks: List<Task> = emptyList()
)
