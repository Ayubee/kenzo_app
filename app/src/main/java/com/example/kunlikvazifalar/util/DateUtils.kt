package com.example.kunlikvazifalar.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.US)

    fun getTodayDate(): String {
        return DATE_FORMAT.format(Date())
    }

    fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return DATE_FORMAT.format(calendar.time)
    }

    fun formatUzbekDateHeader(dateStr: String): String {
        val today = getTodayDate()
        val yesterday = getYesterdayDate()

        return try {
            val date = DATE_FORMAT.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val monthIndex = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val monthName = getUzbekMonth(monthIndex)

            when (dateStr) {
                today -> "Bugun, $day-$monthName"
                yesterday -> "Kecha, $day-$monthName"
                else -> "$day-$monthName, $year-yil"
            }
        } catch (_: Exception) {
            dateStr
        }
    }

    fun getUzbekMonth(monthIndex: Int): String {
        return when (monthIndex) {
            0 -> "yanvar"
            1 -> "fevral"
            2 -> "mart"
            3 -> "aprel"
            4 -> "may"
            5 -> "iyun"
            6 -> "iyul"
            7 -> "avgust"
            8 -> "sentabr"
            9 -> "oktabr"
            10 -> "noyabr"
            11 -> "dekabr"
            else -> ""
        }
    }

    /**
     * Belgilangan sana va vaqtdan 5 daqiqa oldingi vaqtni millisekundlarda hisoblash.
     */
    fun calculateReminderTimeMillis(dateStr: String, timeStr: String): Long? {
        return try {
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val date = dateTimeFormat.parse("$dateStr $timeStr") ?: return null
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.MINUTE, -5)
            }
            calendar.timeInMillis
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Belgilangan vaqtgacha 5 daqiqadan kam qolganmi yoki vaqt o'tib ketganmi tekshiradi.
     */
    fun isLessThan5MinutesOrPast(dateStr: String, timeStr: String): Boolean {
        val reminderTime = calculateReminderTimeMillis(dateStr, timeStr) ?: return true
        val currentTime = System.currentTimeMillis()
        return reminderTime <= currentTime
    }

    fun formatTime24(hour: Int, minute: Int): String {
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }
}
