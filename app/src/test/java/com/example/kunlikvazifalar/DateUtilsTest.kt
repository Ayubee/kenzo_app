package com.example.kunlikvazifalar

import com.example.kunlikvazifalar.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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

    @Test
    fun invalidDateOrTimeDoesNotSilentlyScheduleAnotherDay() {
        assertNull(DateUtils.calculateReminderTimeMillis("2026-02-29", "09:00"))
        assertNull(DateUtils.calculateReminderTimeMillis("2026-10-07", "24:00"))
        assertNull(DateUtils.calculateReminderTimeMillis("2026-10-07", "12:60"))
        assertNull(DateUtils.calculateReminderTimeMillis("2026-10-07junk", "09:00"))
        assertNull(DateUtils.calculateReminderTimeMillis("2026-10-07", "09:00junk"))
        assertNotNull(DateUtils.calculateReminderTimeMillis("2028-02-29", "09:00"))
    }

    @Test
    fun midnightReminderFallsOnPreviousDayInCurrentTimezone() {
        val previousZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"))
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            assertEquals(
                format.parse("2026-10-06 23:58")!!.time,
                DateUtils.calculateReminderTimeMillis("2026-10-07", "00:03")
            )
        } finally {
            TimeZone.setDefault(previousZone)
        }
    }

    @Test
    fun changingTimezoneChangesReminderInstant() {
        val previousZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val utc = DateUtils.calculateReminderTimeMillis("2026-10-07", "09:00")!!
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"))
            val tashkent = DateUtils.calculateReminderTimeMillis("2026-10-07", "09:00")!!
            assertEquals(5 * 60 * 60 * 1000L, utc - tashkent)
        } finally {
            TimeZone.setDefault(previousZone)
        }
    }

    @Test
    fun testAddMinutesCumulativeAndWrap() {
        // 14:30 + 15 -> 14:45
        assertEquals(Pair(14, 45), DateUtils.addMinutes(14, 30, 15))

        // 14:45 + 30 -> 15:15
        assertEquals(Pair(15, 15), DateUtils.addMinutes(14, 45, 30))

        // 15:15 + 60 -> 16:15
        assertEquals(Pair(16, 15), DateUtils.addMinutes(15, 15, 60))

        // Midnight roll over: 23:50 + 15 -> 00:05
        assertEquals(Pair(0, 5), DateUtils.addMinutes(23, 50, 15))

        // Midnight roll over: 23:30 + 60 -> 00:30
        assertEquals(Pair(0, 30), DateUtils.addMinutes(23, 30, 60))
    }
}
