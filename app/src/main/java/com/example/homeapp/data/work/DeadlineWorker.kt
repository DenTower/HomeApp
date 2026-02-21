package com.example.homeapp.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.homeapp.domain.HomeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first



// @HiltWorker — это «метка» для системы, чтобы она знала: в этот класс нужно
// автоматически подставить зависимости (например, доступ к базе данных).
@HiltWorker
class DeadlineWorker @AssistedInject constructor(
    // Контекст — это «руки» приложения, через которые оно обращается к системе
    @Assisted private val context: Context,
    // Параметры — техническая информация о том, как именно запущен этот процесс
    @Assisted params: WorkerParameters,
    // Репозиторий — это наш «завхоз», который знает, где лежат данные о задачах
    private val repository: HomeRepository
): CoroutineWorker(context, params) { // CoroutineWorker означает, что задача умеет «ждать» (например, загрузки данных), не замораживая телефон

    // doWork — это «сердце» класса. Весь код внутри выполняется, когда наступает время задачи
    override suspend fun doWork(): Result {

        // Достаем ID задачи, которую нам передали «снаружи».
        // Если ID почему-то нет — говорим системе, что произошла ошибка (failure)
        val taskId = inputData.getString("taskId") ?: return Result.failure()

        // Идем в базу данных через репозиторий, чтобы проверить актуальное состояние задачи
        val allMembers = repository.getFamilyMembers().first()
        // Ищем конкретную задачу в списке всех дел всех членов семьи
        val task = allMembers.flatMap { it.tasks }.find { it.id == taskId }

        // Пишем в лог (специальный журнал для разработчика), что мы нашли
        Log.d("DeadlineWorker", "Задача: ${allMembers.flatMap { it.tasks }}")

        // ПРОВЕРКА: Если задача за это время была удалена или уже отмечена как выполненная —
        // просто завершаем работу успешно, не беспокоя пользователя уведомлением.
        if (task == null || task.isDone) {
            return Result.success()
        }

        // Пытаемся получить заголовок для уведомления. Если его нет — выходим.
        val title = inputData.getString("title") ?: return Result.success()

        // Создаем «канал» (в современных Android уведомления обязательно делятся на группы/каналы)
        createNotificationChannel(context)

        // Показываем само уведомление на экране телефона
        showNotification(title)

        // Сообщаем системе: «Всё прошло успешно!»
        return Result.success()
    }

    // Метод, который «рисует» уведомление в шторке телефона
    private fun showNotification(text: String) {
        // Берем у системы «менеджер уведомлений» — это главный по оповещениям
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        // Собираем само уведомление: заголовок, текст и иконка
        val notification = NotificationCompat.Builder(applicationContext, "deadlines")
            .setContentTitle("Просроченная задача")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Стандартная иконка восклицательного знака
            .build()

        // Отправляем уведомление. В качестве ID используем текущее время, чтобы уведомления не затирали друг друга
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // Настройка «канала» уведомлений (нужно для Android 8.0 и выше)
    private fun createNotificationChannel(context: Context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Напоминания о задачах"
            val descriptionText = "Уведомления о дедлайнах домашних дел"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            // Важно: ID "deadlines" должен совпадать с тем, что мы указали при сборке уведомления выше
            val channel = NotificationChannel("deadlines", name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Регистрируем канал в системе
            notificationManager.createNotificationChannel(channel)
        }
    }
}
