package com.euiyoul.todo.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormat = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
private val dateFormat = DateTimeFormatter.ofPattern("M/d(E)", Locale.KOREAN)
private val dateWithYearFormat = DateTimeFormatter.ofPattern("yyyy/M/d(E)", Locale.KOREAN)

/** 마감 일시를 "오늘 오후 2:00", "내일 오전 9:00", "11/7(금) 오후 6:00" 형태로 만든다. */
fun formatDue(millis: Long, zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): String {
    val dateTime = Instant.ofEpochMilli(millis).atZone(zone)
    val date = dateTime.toLocalDate()
    val dayPart = when (date) {
        today -> "오늘"
        today.plusDays(1) -> "내일"
        today.minusDays(1) -> "어제"
        else -> if (date.year == today.year) dateFormat.format(date) else dateWithYearFormat.format(date)
    }
    return "$dayPart ${timeFormat.format(dateTime)}"
}

fun formatDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    dateWithYearFormat.format(Instant.ofEpochMilli(millis).atZone(zone))

fun formatTime(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    timeFormat.format(Instant.ofEpochMilli(millis).atZone(zone))
