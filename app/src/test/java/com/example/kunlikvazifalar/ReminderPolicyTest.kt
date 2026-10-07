package com.example.kunlikvazifalar

import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.notification.ReminderKind
import com.example.kunlikvazifalar.notification.ReminderPolicy
import org.junit.Assert.*
import org.junit.Test

class ReminderPolicyTest {
    private val task = Task(id = 7, text = "Task", date = "2026-10-07", time = "12:00")
    private val due = ReminderPolicy.dueAt(task, ReminderKind.BEFORE)!!
    private fun pending(now: Long, repeats: Boolean = true, sent: Set<String> = emptySet(), value: Task = task) =
        ReminderPolicy.pending(value, repeats, sent, now, "2026-10-07")

    @Test fun keepsFiveMinutesBeforeAndExactlyTwoRepeats() {
        val plans = pending(due - 1)
        assertEquals(ReminderKind.entries, plans.map { it.kind })
        assertEquals(listOf(due, due + 15 * 60_000, due + 35 * 60_000), plans.map { it.at })
    }
    @Test fun disabledRepeatsLeaveOnlyOriginalReminder() {
        assertEquals(listOf(ReminderKind.BEFORE), pending(due - 1, repeats = false).map { it.kind })
    }
    @Test fun restorationSkipsPastAndAlreadyDeliveredSlots() {
        assertEquals(listOf(ReminderKind.REPEAT_30), pending(due + 16 * 60_000).map { it.kind })
        assertTrue(pending(due + 16 * 60_000, sent = setOf("REPEAT_30")).isEmpty())
        assertTrue(pending(due + 36 * 60_000).isEmpty())
    }
    @Test fun completedUntimedInvalidAndHistoricalNeverSchedule() {
        for (value in listOf(task.copy(isCompleted = true), task.copy(time = null), task.copy(time = "25:01"), task.copy(date = "2026-10-06"))) {
            assertTrue(pending(due - 1, value = value).isEmpty())
        }
    }
    @Test fun receiverRejectsEarlyStaleCompletedAndDisabledDeliveries() {
        val kind = ReminderKind.REPEAT_10
        val at = ReminderPolicy.dueAt(task, kind)!!
        assertTrue(ReminderPolicy.canDeliver(task, kind, true, at, at + 1, task.date))
        assertFalse(ReminderPolicy.canDeliver(task, kind, true, at, at - 1, task.date))
        assertFalse(ReminderPolicy.canDeliver(task.copy(time = "13:00"), kind, true, at, at, task.date))
        assertFalse(ReminderPolicy.canDeliver(task.copy(isCompleted = true), kind, true, at, at, task.date))
        assertFalse(ReminderPolicy.canDeliver(task, kind, false, at, at, task.date))
        assertFalse(ReminderPolicy.canDeliver(task, kind, true, at, at, "2026-10-08"))
    }
    @Test fun delayedAlarmsCannotBurstTogether() {
        val last = ReminderPolicy.dueAt(task, ReminderKind.REPEAT_30)!!
        assertFalse(ReminderPolicy.canDeliver(task, ReminderKind.BEFORE, true, due, last, task.date))
        assertFalse(ReminderPolicy.canDeliver(task, ReminderKind.REPEAT_10, true, due + 15 * 60_000, last, task.date))
        assertTrue(ReminderPolicy.canDeliver(task, ReminderKind.REPEAT_30, true, last, last, task.date))
        assertFalse(ReminderPolicy.canDeliver(task, ReminderKind.REPEAT_30, true, last, last + 20 * 60_000, task.date))
    }
}
