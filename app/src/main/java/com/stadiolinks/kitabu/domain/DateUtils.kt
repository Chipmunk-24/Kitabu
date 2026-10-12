package com.stadiolinks.kitabu.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

/** Whole calendar days from today until the deadline (negative when overdue). */
fun daysRemaining(deadlineMillis: Long, nowMillis: Long = System.currentTimeMillis()): Long {
    val zone = ZoneId.systemDefault()
    val deadline = Instant.ofEpochMilli(deadlineMillis).atZone(zone).toLocalDate()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return ChronoUnit.DAYS.between(today, deadline)
}

fun formatDate(millis: Long): String =
    dateFormatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
