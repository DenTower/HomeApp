package com.example.homeapp.presentation

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.homeapp.domain.model.FamilyMember
import com.example.homeapp.presentation.utils.formatDeadline
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    var showAddMemberDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Лаунчер для запроса разрешения
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Можно показать Toast, что без этого напоминания не придут
        }
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddMemberDialog = true }) {
                Icon(Icons.Default.PersonAdd, null)
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "💡 Совет дня: ${state.dailyAdvice}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(state.members, key = { it.id }) { member ->
                MemberCard(
                    member = member,
                    onDelete = { viewModel.removeMember(member.id) },
                    onAddTask = { title, deadline ->
                        viewModel.addTask(member.id, title, deadline)
                    },
                    onDeleteTask = { taskId ->
                        viewModel.removeTask(member.id, taskId)
                    },
                    onToggleTask = { taskId ->
                        viewModel.toggleTask(member.id, taskId)
                    }

                )
            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAdd = {
                viewModel.addMember(it)
                showAddMemberDialog = false
            }
        )
    }
}

@Composable
fun MemberCard(
    member: FamilyMember,
    onDelete: () -> Unit,
    onAddTask: (String, LocalDateTime) -> Unit,
    onDeleteTask: (String) -> Unit,
    onToggleTask: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null)
                }
            }

            Spacer(Modifier.height(8.dp))

            // 1. Показываем только НЕВЫПОЛНЕННЫЕ задачи в основном списке
            member.tasks.filter { !it.isDone }.forEach { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { onToggleTask(task.id) }
                    )

                    Column(Modifier.weight(1f)) {
                        Text(task.title) // Убрали TextDecoration, так как тут только активные задачи

                        Text(
                            "До ${formatDeadline(task.deadline)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(onClick = { onDeleteTask(task.id) }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { showDialog = true }) {
                    Text("Добавить задачу")
                }

                TextButton(onClick = { showHistory = true }) {
                    Text("История")
                }
            }
        }
    }

    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },
            onAdd = { title, deadline ->
                onAddTask(title, deadline)
                showDialog = false
            }
        )
    }

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
                // Желательно добавить Modifier.verticalScroll(rememberScrollState()),
                // если история может быть длинной
                Column {
                    // 2. Показываем ВЫПОЛНЕННЫЕ задачи с возможностью отменить выполнение
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
                                    textDecoration = TextDecoration.LineThrough,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                    if (member.tasks.none { it.isDone }) {
                        Text("История пуста", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        )
    }
}

@Composable
fun AddMemberDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onAdd(name) }) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        title = { Text("Новый член семьи") },
        text = {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Имя") })
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, LocalDateTime) -> Unit
) {
    var title by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank()
                        && dateState.selectedDateMillis != null,
                onClick = {

                    val millis = dateState.selectedDateMillis ?: return@TextButton

                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()

                    val dateTime = LocalDateTime.of(
                        date,
                        LocalTime.of(timeState.hour, timeState.minute)
                    )

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

                LaunchedEffect(dateState.selectedDateMillis) {
                    if (dateState.selectedDateMillis != null) {
                        showDatePicker = false
                    }
                }

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название задачи") }
                )

                Spacer(Modifier.height(12.dp))

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

                if (showDatePicker) {
                    DatePicker(
                        state = dateState,
                        showModeToggle = false,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(onClick = { showTimePicker = !showTimePicker }) {
                    Text("Время: %02d:%02d".format(timeState.hour, timeState.minute))
                }

                if (showTimePicker) {
                    TimePicker(state = timeState)
                }
            }
        }
    )
}



