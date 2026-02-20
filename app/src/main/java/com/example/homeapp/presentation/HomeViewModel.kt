package com.example.homeapp.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.homeapp.data.work.DeadlineWorker
import com.example.homeapp.domain.HomeRepository
import com.example.homeapp.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val workManager: WorkManager
): ViewModel() {

    private val _dailyAdvice = MutableStateFlow("Загрузка полезного совета...")

    val state = repository.getFamilyMembers()
        .combine(_dailyAdvice) { members, advice ->
            HomeState(
                members = members,
                dailyAdvice = advice
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )

    init {
        loadAdvice()
    }

    private fun loadAdvice() {
        viewModelScope.launch {
            repository.getRandomAdvice()
                .onSuccess { advice ->
                    _dailyAdvice.value = advice.text
                }
                .onFailure {
                    _dailyAdvice.value = "Не удалось загрузить совет, но ты всё равно молодец!"
                }
        }
    }

    fun addMember(name: String) {
        viewModelScope.launch {
            repository.addMember(name)
        }
    }

    fun removeMember(id: String) {
        viewModelScope.launch {
            repository.removeMember(id)
        }
    }

    fun addTask(memberId: String, title: String, deadline: LocalDateTime) {
        viewModelScope.launch {
            val task = Task(title = title, deadline = deadline)
            repository.addTask(memberId, task)
            scheduleNotification(task)
        }
    }

    fun removeTask(
        memberId: String,
        taskId: String
    ) { // memberId тут уже не нужен, но оставил для совместимости с вашим UI
        viewModelScope.launch {
            repository.removeTask(taskId)
        }
    }

    fun toggleTask(memberId: String, taskId: String) {
        // Чтобы переключить статус, нужно знать текущий.
        // Ищем задачу в текущем state:
        val task = state.value.members
            .flatMap { it.tasks }
            .find { it.id == taskId } ?: return

        val newStatus = !task.isDone
        val completedAt = if(newStatus) LocalDateTime.now() else null

        viewModelScope.launch {
            repository.toggleTask(taskId, newStatus, completedAt)
        }
    }

    fun scheduleNotification(task: Task) {
        val delay = Duration.between(
            LocalDateTime.now(),
            task.deadline
        ).toMillis()

        if(delay <= 0) return

        val data = workDataOf(
            "title" to task.title,
            "taskId" to task.id
        )

        val work = OneTimeWorkRequestBuilder<DeadlineWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueue(work)
    }
}