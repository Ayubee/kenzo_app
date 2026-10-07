package com.example.kunlikvazifalar

import com.example.kunlikvazifalar.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class DateUtilsTest {

    @Test
    fun testGetTodayDateFormat() {
        val today = DateUtils.getTodayDate()
        assertNotNull(today)
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun testFormatUzbekDateHeader() {
        val today = DateUtils.getTodayDate()
        val formattedToday = DateUtils.formatUzbekDateHeader(today)
        assertTrue(formattedToday.startsWith("Bugun, "))

        val yesterday = DateUtils.getYesterdayDate()
        val formattedYesterday = DateUtils.formatUzbekDateHeader(yesterday)
        assertTrue(formattedYesterday.startsWith("Kecha, "))
    }

    @Test
    fun testCalculateReminderTimeMillisIs5MinutesBefore() {
        val dateStr = "2026-10-10"
        val timeStr = "15:00"

        val reminderMillis = DateUtils.calculateReminderTimeMillis(dateStr, timeStr)
        assertNotNull(reminderMillis)

        val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val eventMillis = fullFormat.parse("$dateStr $timeStr")!!.time

        // 5 daqiqa = 300,000 millisekund
        assertEquals(300_000L, eventMillis - reminderMillis!!)
    }

    @Test
    fun testIsLessThan5MinutesOrPast() {
        // O'tgan sana
        assertTrue(DateUtils.isLessThan5MinutesOrPast("2020-01-01", "10:00"))

        // Uzoq kelajakdagi sana
        assertFalse(DateUtils.isLessThan5MinutesOrPast("2099-01-01", "12:00"))
    }

    @Test
    fun testFormatTime24() {
        assertEquals("09:05", DateUtils.formatTime24(9, 5))
        assertEquals("14:30", DateUtils.formatTime24(14, 30))
        assertEquals("00:00", DateUtils.formatTime24(0, 0))
    }
}
