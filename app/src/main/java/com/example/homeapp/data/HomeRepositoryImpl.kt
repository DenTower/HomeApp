package com.example.homeapp.data

import com.example.homeapp.domain.HomeRepository
import com.example.homeapp.data.local.FamilyDao
import com.example.homeapp.data.local.MemberEntity
import com.example.homeapp.data.local.TaskEntity
import com.example.homeapp.data.remote.AdviceApi
import com.example.homeapp.domain.model.Advice
import com.example.homeapp.domain.model.FamilyMember
import com.example.homeapp.domain.model.Task
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class HomeRepositoryImpl @Inject constructor(
    private val dao: FamilyDao,
    private val api: AdviceApi
): HomeRepository {

    override fun getFamilyMembers(): Flow<List<FamilyMember>> {
        return dao.getMembersWithTasks().map { list ->
            list.map { item ->
                FamilyMember(
                    id = item.member.id,
                    name = item.member.name,
                    tasks = item.tasks.map { taskEntity ->
                        Task(
                            id = taskEntity.id,
                            title = taskEntity.title,
                            deadline = millisToDateTime(taskEntity.deadline),
                            isDone = taskEntity.isDone,
                            completedAt = taskEntity.completedAt?.let { millisToDateTime(it) }
                        )
                    }
                )
            }
        }
    }

    override suspend fun addMember(name: String) {
        dao.insertMember(MemberEntity(id = UUID.randomUUID().toString(), name = name))
    }

    override suspend fun removeMember(id: String) {
        dao.deleteMember(id)
    }

    override suspend fun addTask(memberId: String, task: Task) {
        dao.insertTask(
            TaskEntity(
                id = task.id,
                memberId = memberId,
                title = task.title,
                deadline = dateTimeToMillis(task.deadline),
                isDone = false,
                completedAt = null
            )
        )
    }

    override suspend fun removeTask(taskId: String) {
        dao.deleteTask(taskId)
    }

    override suspend fun toggleTask(taskId: String, isDone: Boolean, completedAt: LocalDateTime?) {
        dao.updateTaskStatus(
            taskId = taskId,
            isDone = isDone,
            completedAt = completedAt?.let { dateTimeToMillis(it) }
        )
    }

    override suspend fun getRandomAdvice(): Result<Advice> = runCatching {
        val response = api.fetchRandomAdvice()
        Advice(response.slip.advice)
    }

    // Вспомогательные функции для конвертации времени
    private fun dateTimeToMillis(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun millisToDateTime(millis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
}