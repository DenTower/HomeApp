package com.example.homeapp.presentation.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDeadline(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern(
        "d MMMM HH:mm",
        Locale("ru")
    )
    return dateTime.format(formatter)
}
