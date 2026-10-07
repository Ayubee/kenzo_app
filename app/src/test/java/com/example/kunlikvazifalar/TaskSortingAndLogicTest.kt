package com.example.kunlikvazifalar

import com.example.kunlikvazifalar.data.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskSortingAndLogicTest {

    @Test
    fun testSortingTimedFirstThenUntimed() {
        val tasks = listOf(
            Task(id = 1, text = "Vaqtsiz vazifa 1", date = "2026-10-07", time = null),
            Task(id = 2, text = "Tushlik", date = "2026-10-07", time = "13:00"),
            Task(id = 3, text = "Ertalabki yugurish", date = "2026-10-07", time = "07:30"),
            Task(id = 4, text = "Kechki uchrashuv", date = "2026-10-07", time = "19:00"),
            Task(id = 5, text = "Vaqtsiz vazifa 2", date = "2026-10-07", time = null)
        )

        // Comparator simulates SQLite:
        // CASE WHEN time IS NULL OR time = '' THEN 1 ELSE 0 END ASC, time ASC, id ASC
        val comparator = Comparator<Task> { t1, t2 ->
            val hasTime1 = if (t1.time.isNullOrBlank()) 1 else 0
            val hasTime2 = if (t2.time.isNullOrBlank()) 1 else 0
            if (hasTime1 != hasTime2) {
                hasTime1.compareTo(hasTime2)
            } else if (hasTime1 == 0) {
                t1.time!!.compareTo(t2.time!!)
            } else {
                t1.id.compareTo(t2.id)
            }
        }

        val sorted = tasks.sortedWith(comparator)

        // 1. Ertalabki yugurish (07:30)
        // 2. Tushlik (13:00)
        // 3. Kechki uchrashuv (19:00)
        // 4. Vaqtsiz vazifa 1
        // 5. Vaqtsiz vazifa 2
        assertEquals("Ertalabki yugurish", sorted[0].text)
        assertEquals("Tushlik", sorted[1].text)
        assertEquals("Kechki uchrashuv", sorted[2].text)
        assertEquals("Vaqtsiz vazifa 1", sorted[3].text)
        assertEquals("Vaqtsiz vazifa 2", sorted[4].text)
    }

    @Test
    fun testReAddKeepsOriginalAndCreatesUncompletedCopy() {
        val originalTask = Task(
            id = 42,
            text = "Eski kun vazifasi",
            date = "2026-10-05",
            time = "10:00",
            isCompleted = true
        )

        val newTodayDate = "2026-10-07"
        val newTime = "14:00"

        val reAddedTask = Task(
            id = 0, // Yangi generatsiya qilinadigan ID
            text = originalTask.text,
            date = newTodayDate,
            time = newTime,
            isCompleted = false
        )

        // Original o'zgarmagan bo'lishi kerak
        assertEquals(42L, originalTask.id)
        assertEquals("2026-10-05", originalTask.date)
        assertTrue(originalTask.isCompleted)

        // Yangi nusxa bugun uchun, bajarilmagan
        assertEquals("Eski kun vazifasi", reAddedTask.text)
        assertEquals("2026-10-07", reAddedTask.date)
        assertEquals("14:00", reAddedTask.time)
        assertFalse(reAddedTask.isCompleted)
    }

    @Test
    fun testHistoryGroupingByDate() {
        val tasks = listOf(
            Task(id = 1, text = "A", date = "2026-10-06"),
            Task(id = 2, text = "B", date = "2026-10-06"),
            Task(id = 3, text = "C", date = "2026-10-05")
        )

        val grouped = tasks.groupBy { it.date }
        assertEquals(2, grouped.size)
        assertEquals(2, grouped["2026-10-06"]?.size)
        assertEquals(1, grouped["2026-10-05"]?.size)
    }
}
