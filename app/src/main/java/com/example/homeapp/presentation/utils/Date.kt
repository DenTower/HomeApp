package com.example.homeapp.presentation.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Функция форматирования даты дедлайна в читаемый вид
// Например: 5 марта 14:30
fun formatDeadline(dateTime: LocalDateTime): String {

    // Шаблон форматирования даты
    // d = день
    // MMMM = месяц словами
    // HH:mm = часы:минуты
    val formatter = DateTimeFormatter.ofPattern(
        "d MMMM HH:mm",
        Locale("ru") // русская локаль для русских названий месяцев
    )

    // Преобразуем дату в строку
    return dateTime.format(formatter)
}
