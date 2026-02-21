package com.example.homeapp.presentation

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

// @Composable — специальная функция Jetpack Compose.
// Она описывает UI (интерфейс), а не выполняет действия.
// Compose сам вызывает её при изменении состояния.
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {

    // Подписываемся на состояние из ViewModel.
    // collectAsState превращает Flow → State,
    // чтобы UI автоматически обновлялся при изменениях.
    val state by viewModel.state.collectAsState()

    // Локальное состояние UI:
    // нужно ли показывать диалог добавления пользователя.
    // remember сохраняет значение между перерисовками UI.
    var showAddMemberDialog by remember { mutableStateOf(false) }


    // Лаунчер — специальный объект для запроса разрешений Android.
    // Android не даёт доступ к опасным функциям без разрешения пользователя.
    val permissionLauncher = rememberLauncherForActivityResult(

        // Тип запроса — запрос одного разрешения
        contract = ActivityResultContracts.RequestPermission()

    ) { isGranted ->

        // Этот блок выполнится после ответа пользователя.
        if (!isGranted) {
            // Можно показать сообщение, что уведомления работать не будут
        }
    }


    // LaunchedEffect — корутина, которая запускается
    // когда Composable появляется на экране.
    LaunchedEffect(Unit) {

        // Проверяем версию Android.
        // Начиная с Android 13 нужно отдельно разрешение на уведомления.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // Запускаем запрос разрешения
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    // Scaffold — базовый layout Material Design.
    // Он автоматически расставляет:
    // - кнопки
    // - панели
    // - отступы
    // - floating action button
    Scaffold(

        // Плавающая кнопка внизу справа
        floatingActionButton = {

            FloatingActionButton(onClick = { showAddMemberDialog = true }) {

                // Иконка "добавить человека"
                Icon(Icons.Default.PersonAdd, null)
            }
        }

    ) { padding ->

        // LazyColumn — список с ленивой загрузкой элементов.
        // Аналог RecyclerView в старом Android.
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // Первый элемент списка — карточка с советом дня
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


            // items — список элементов.
            // Compose автоматически создаёт UI для каждого.
            items(state.members, key = { it.id }) { member ->

                // Карточка участника семьи
                MemberCard(
                    member = member,

                    // Удаление участника
                    onDelete = { viewModel.removeMember(member.id) },

                    // Добавление задачи
                    onAddTask = { title, deadline ->
                        viewModel.addTask(member.id, title, deadline)
                    },

                    // Удаление задачи
                    onDeleteTask = { taskId ->
                        viewModel.removeTask(member.id, taskId)
                    },

                    // Отметить задачу выполненной
                    onToggleTask = { taskId ->
                        viewModel.toggleTask(member.id, taskId)
                    }
                )
            }
        }
    }


    // Если флаг true — показываем диалог
    if (showAddMemberDialog) {
        AddMemberDialog(

            // Закрыть диалог
            onDismiss = { showAddMemberDialog = false },

            // Добавить участника
            onAdd = {
                viewModel.addMember(it)
                showAddMemberDialog = false
            }
        )
    }
}



@Composable
fun AddMemberDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {

    // Локальное состояние поля ввода имени
    var name by remember { mutableStateOf("") }

    // AlertDialog — готовый компонент диалогового окна
    AlertDialog(

        // Если пользователь нажал вне окна
        onDismissRequest = onDismiss,

        // Кнопка подтверждения
        confirmButton = {
            TextButton(onClick = { onAdd(name) }) {
                Text("Добавить")
            }
        },

        // Кнопка отмены
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },

        // Заголовок окна
        title = { Text("Новый член семьи") },

        // Основное содержимое окна
        text = {
            // Поле ввода текста
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") }
            )
        }
    )
}
