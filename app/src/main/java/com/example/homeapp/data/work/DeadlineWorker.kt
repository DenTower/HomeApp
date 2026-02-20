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

@HiltWorker
class DeadlineWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: HomeRepository
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val taskId = inputData.getString("taskId") ?: return Result.failure()

        // Получаем все данные (нужно добавить метод в репозиторий для поиска одной задачи)
        val allMembers = repository.getFamilyMembers().first()
        val task = allMembers.flatMap { it.tasks }.find { it.id == taskId }
        Log.d("DeadlineWorker", "Задача: ${allMembers.flatMap { it.tasks }}")

        // ПРОВЕРКА: Если задача удалена или уже выполнена — ничего не показываем
        if (task == null || task.isDone) {
            return Result.success()
        }


        val title = inputData.getString("title") ?: return Result.success()

        createNotificationChannel(context)

        showNotification(title)

        return Result.success()
    }

    private fun showNotification(text: String) {
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, "deadlines")
            .setContentTitle("Просроченная задача")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Напоминания о задачах"
            val descriptionText = "Уведомления о дедлайнах домашних дел"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            // channelId должен СТРОГО совпадать с тем, что вы указали в NotificationCompat.Builder
            val channel = NotificationChannel("deadlines", name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
