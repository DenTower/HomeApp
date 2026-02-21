package com.example.homeapp.domain

import com.example.homeapp.domain.model.Advice
import com.example.homeapp.domain.model.FamilyMember
import com.example.homeapp.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

// Интерфейс репозитория — контракт между ViewModel и источниками данных
// ViewModel НЕ знает, откуда данные — из базы, сети или файла
interface HomeRepository {

    // Flow — поток данных
    // UI будет автоматически получать обновления,
    // когда данные изменятся в базе
    fun getFamilyMembers(): Flow<List<FamilyMember>>

    // Добавление члена семьи
    suspend fun addMember(name: String)

    // Удаление члена семьи
    suspend fun removeMember(id: String)

    // Добавление задачи
    suspend fun addTask(memberId: String, task: Task)

    // Удаление задачи
    suspend fun removeTask(taskId: String)

    // Переключение статуса задачи (выполнена/не выполнена)
    suspend fun toggleTask(
        taskId: String,
        isDone: Boolean,
        completedAt: LocalDateTime?
    )

    // Получение случайного совета из интернета
    suspend fun getRandomAdvice(): Result<Advice>
}