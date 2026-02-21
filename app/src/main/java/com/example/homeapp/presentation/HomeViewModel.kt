package com.example.homeapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.homeapp.data.work.DeadlineWorker
import com.example.homeapp.domain.HomeRepository
import com.example.homeapp.domain.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

// Аннотация говорит Hilt (библиотеке внедрения зависимостей),
// что этот класс — ViewModel и его зависимости нужно создать автоматически.
@HiltViewModel
class HomeViewModel @Inject constructor(

    // Репозиторий — слой доступа к данным (БД, сеть и т.п.)
    private val repository: HomeRepository,

    // WorkManager — системный планировщик фоновых задач Android.
    private val workManager: WorkManager

) : ViewModel() {   // ViewModel — класс, который хранит состояние экрана

    // Внутренний поток состояния для "совета дня".
    // MutableStateFlow — реактивная переменная (observable state).
    private val _dailyAdvice = MutableStateFlow("Загрузка полезного совета...")


    // Публичное состояние экрана.
    // UI будет подписываться на него и автоматически обновляться.
    val state = repository.getFamilyMembers()

        // combine объединяет два потока:
        // список участников + текст совета
        .combine(_dailyAdvice) { members, advice ->

            // Создаём единый объект состояния экрана
            HomeState(
                members = members,
                dailyAdvice = advice
            )
        }

        // stateIn превращает поток в StateFlow — специальный поток состояния.
        .stateIn(
            scope = viewModelScope, // жизненный цикл привязан к ViewModel
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeState()
        )


    // init вызывается автоматически при создании ViewModel
    init {
        loadAdvice()
    }


    // Загружает совет с сервера
    private fun loadAdvice() {

        // viewModelScope — coroutine scope,
        // автоматически отменяется когда ViewModel уничтожается
        viewModelScope.launch {

            repository.getRandomAdvice()

                // если запрос успешен
                .onSuccess { advice ->
                    _dailyAdvice.value = advice.text
                }

                // если произошла ошибка
                .onFailure {
                    _dailyAdvice.value =
                        "Не удалось загрузить совет, но ты всё равно молодец!"
                }
        }
    }


    // Добавить участника семьи
    fun addMember(name: String) {
        viewModelScope.launch {
            repository.addMember(name)
        }
    }


    // Удалить участника
    fun removeMember(id: String) {
        viewModelScope.launch {
            repository.removeMember(id)
        }
    }


    // Добавить задачу
    fun addTask(memberId: String, title: String, deadline: LocalDateTime) {
        viewModelScope.launch {

            // создаём объект задачи
            val task = Task(title = title, deadline = deadline)

            // сохраняем в базе
            repository.addTask(memberId, task)

            // планируем уведомление
            scheduleNotification(task)
        }
    }


    // Удалить задачу
    fun removeTask(
        memberId: String,
        taskId: String
    ) {
        // memberId уже не нужен, но оставлен ради совместимости интерфейса
        viewModelScope.launch {
            repository.removeTask(taskId)
        }
    }


    // Переключить статус задачи (выполнена / не выполнена)
    fun toggleTask(memberId: String, taskId: String) {

        // Нужно найти текущую задачу в текущем состоянии экрана
        val task = state.value.members
            .flatMap { it.tasks }  // объединяем все списки задач всех людей
            .find { it.id == taskId }
            ?: return              // если не нашли — выходим


        // меняем статус на противоположный
        val newStatus = !task.isDone

        // если задача выполнена — сохраняем время выполнения
        val completedAt =
            if (newStatus) LocalDateTime.now()
            else null


        viewModelScope.launch {
            repository.toggleTask(taskId, newStatus, completedAt)
        }
    }


    // Планирование уведомления к дедлайну
    fun scheduleNotification(task: Task) {

        // считаем сколько миллисекунд до дедлайна
        val delay = Duration.between(
            LocalDateTime.now(),
            task.deadline
        ).toMillis()

        // если дедлайн уже прошёл — ничего не делаем
        if(delay <= 0) return


        // Данные, которые передадутся в Worker
        val data = workDataOf(
            "title" to task.title,
            "taskId" to task.id
        )


        // OneTimeWorkRequest — задача, которая выполнится один раз
        val work = OneTimeWorkRequestBuilder<DeadlineWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) // отложенный запуск
            .setInputData(data) // передаём данные
            .build()


        // Добавляем задачу в очередь WorkManager
        workManager.enqueue(work)
    }
}