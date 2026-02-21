package com.example.homeapp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.homeapp.domain.model.FamilyMember
import com.example.homeapp.presentation.utils.formatDeadline
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

// Composable-функция — это функция, которая описывает UI.
// Она НЕ рисует интерфейс напрямую, а описывает как он должен выглядеть.
@Composable
fun MemberCard(

    // Данные одного человека
    member: FamilyMember,

    // Колбэки — функции, которые передаются сверху (из родительского экрана)
    // UI сам ничего не делает с данными — он сообщает ViewModel что нужно сделать.
    onDelete: () -> Unit,
    onAddTask: (String, LocalDateTime) -> Unit,
    onDeleteTask: (String) -> Unit,
    onToggleTask: (String) -> Unit
) {

    // Локальное состояние UI: показывать ли диалог создания задачи
    var showDialog by remember { mutableStateOf(false) }

    // Локальное состояние UI: показывать ли окно истории
    var showHistory by remember { mutableStateOf(false) }


    // Card — готовый компонент Material Design.
    // Это просто контейнер с фоном, тенью и скруглением.
    Card(
        modifier = Modifier
            .fillMaxWidth()      // занять всю ширину
            .padding(12.dp),     // внешний отступ
        elevation = CardDefaults.cardElevation(6.dp) // тень
    ) {

        // Column — вертикальный layout
        Column(Modifier.padding(16.dp)) {


            // Row — горизонтальный layout
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Имя пользователя
                Text(member.name, style = MaterialTheme.typography.titleLarge)

                // Spacer с weight = "растягивающийся пробел"
                // отталкивает кнопку удаления вправо
                Spacer(Modifier.weight(1f))

                // Кнопка удаления участника
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null)
                }
            }


            Spacer(Modifier.height(8.dp))


            // Показываем только НЕ выполненные задачи
            member.tasks
                .filter { !it.isDone }
                .forEach { task ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Чекбокс выполнения задачи
                        Checkbox(
                            checked = task.isDone,
                            onCheckedChange = { onToggleTask(task.id) }
                        )

                        // Column с текстом задачи
                        Column(Modifier.weight(1f)) {

                            // Название задачи
                            Text(task.title)

                            // Дедлайн
                            Text(
                                "До ${formatDeadline(task.deadline)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Кнопка удаления задачи
                        IconButton(onClick = { onDeleteTask(task.id) }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                }


            Spacer(Modifier.height(8.dp))


            // Нижняя строка кнопок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // Открыть диалог добавления задачи
                Button(onClick = { showDialog = true }) {
                    Text("Добавить задачу")
                }

                // Открыть историю выполненных задач
                TextButton(onClick = { showHistory = true }) {
                    Text("История")
                }
            }
        }
    }


    // Если showDialog == true → показываем диалог
    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },

            // Когда пользователь нажал "Добавить"
            onAdd = { title, deadline ->
                onAddTask(title, deadline)
                showDialog = false
            }
        )
    }


    // Диалог истории выполненных задач
    if (showHistory) {

        AlertDialog(
            onDismissRequest = { showHistory = false },

            confirmButton = {
                TextButton({ showHistory = false }) {
                    Text("Закрыть")
                }
            },

            title = { Text("Выполненные задачи") },

            text = {

                Column {

                    // Показываем выполненные задачи
                    member.tasks
                        .filter { it.isDone }
                        .sortedByDescending { it.completedAt }
                        .forEach { task ->

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { onToggleTask(task.id) }
                                )

                                Text(
                                    text = task.title,

                                    // зачёркнутый текст — визуальный индикатор выполнения
                                    textDecoration = TextDecoration.LineThrough,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                    // Если выполненных задач нет
                    if (member.tasks.none { it.isDone }) {
                        Text(
                            "История пуста",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, LocalDateTime) -> Unit
) {

    // Текст задачи
    var title by remember { mutableStateOf("") }

    // Показывать ли календарь
    var showDatePicker by remember { mutableStateOf(false) }

    // Показывать ли выбор времени
    var showTimePicker by remember { mutableStateOf(false) }


    // Состояния компонентов выбора даты/времени
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState()


    AlertDialog(
        onDismissRequest = onDismiss,


        // Кнопка подтверждения
        confirmButton = {
            TextButton(

                // Кнопка активна только если введено имя и выбрана дата
                enabled = title.isNotBlank()
                        && dateState.selectedDateMillis != null,

                onClick = {

                    // Получаем выбранную дату в миллисекундах
                    val millis = dateState.selectedDateMillis ?: return@TextButton

                    // Конвертируем millis → LocalDate
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    // Соединяем дату и время
                    val dateTime = LocalDateTime.of(
                        date,
                        LocalTime.of(timeState.hour, timeState.minute)
                    )

                    // Передаём результат наружу
                    onAdd(title, dateTime)
                }
            ) { Text("Добавить") }
        },


        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },


        title = { Text("Новая задача") },


        text = {
            Column {

                // Когда пользователь выбрал дату —
                // автоматически закрываем календарь
                LaunchedEffect(dateState.selectedDateMillis) {
                    if (dateState.selectedDateMillis != null) {
                        showDatePicker = false
                    }
                }


                // Поле ввода названия задачи
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название задачи") }
                )

                Spacer(Modifier.height(12.dp))


                // Кнопка выбора даты
                Button(onClick = { showDatePicker = !showDatePicker }) {
                    Text(
                        dateState.selectedDateMillis?.let {

                            val date = Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            "Дата: $date"
                        } ?: "Выбрать дату"
                    )
                }


                // Сам календарь
                if (showDatePicker) {
                    DatePicker(
                        state = dateState,
                        showModeToggle = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                Spacer(Modifier.height(8.dp))


                // Кнопка выбора времени
                Button(onClick = { showTimePicker = !showTimePicker }) {
                    Text("Время: %02d:%02d".format(timeState.hour, timeState.minute))
                }


                // Виджет выбора времени
                if (showTimePicker) {
                    TimePicker(state = timeState)
                }
            }
        }
    )
}