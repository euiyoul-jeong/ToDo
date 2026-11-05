package com.euiyoul.todo.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DueFormatTest {
    private val zone = ZoneId.of("Asia/Seoul")
    private val today = LocalDate.of(2026, 11, 5)

    private fun millis(dateTime: LocalDateTime) = dateTime.atZone(zone).toInstant().toEpochMilli()

    @Test
    fun relativeDays() {
        assertEquals("오늘 오후 2:00", formatDue(millis(LocalDateTime.of(2026, 11, 5, 14, 0)), zone, today))
        assertEquals("내일 오전 9:30", formatDue(millis(LocalDateTime.of(2026, 11, 6, 9, 30)), zone, today))
        assertEquals("어제 오후 6:00", formatDue(millis(LocalDateTime.of(2026, 11, 4, 18, 0)), zone, today))
    }

    @Test
    fun otherDates() {
        assertEquals("11/7(토) 오후 6:00", formatDue(millis(LocalDateTime.of(2026, 11, 7, 18, 0)), zone, today))
        assertEquals("2027/1/2(토) 오전 8:00", formatDue(millis(LocalDateTime.of(2027, 1, 2, 8, 0)), zone, today))
    }
}
