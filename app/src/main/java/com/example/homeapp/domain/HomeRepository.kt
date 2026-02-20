package com.example.homeapp.domain

import com.example.homeapp.domain.model.Advice
import com.example.homeapp.domain.model.FamilyMember
import com.example.homeapp.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface HomeRepository {
    // Получаем список всех членов семьи с их задачами в виде Flow
    fun getFamilyMembers(): Flow<List<FamilyMember>>

    suspend fun addMember(name: String)
    suspend fun removeMember(id: String)
    suspend fun addTask(memberId: String, task: Task)
    suspend fun removeTask(taskId: String)
    suspend fun toggleTask(taskId: String, isDone: Boolean, completedAt: LocalDateTime?)

    suspend fun getRandomAdvice(): Result<Advice>
}