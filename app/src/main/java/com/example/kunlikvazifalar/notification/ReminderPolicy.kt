package com.example.kunlikvazifalar.notification

import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.util.DateUtils

enum class ReminderKind(val offsetMinutes: Int) {
    BEFORE(-5), REPEAT_10(10), REPEAT_30(30);
    val isRepeat get() = this != BEFORE
}

data class PlannedReminder(val kind: ReminderKind, val at: Long)

/** Pure policy shared by scheduling, receiver validation and regression tests. */
object ReminderPolicy {
    fun dueAt(task: Task, kind: ReminderKind): Long? {
        val before = task.time?.let { DateUtils.calculateReminderTimeMillis(task.date, it) } ?: return null
        return before + (kind.offsetMinutes + 5) * 60_000L
    }

    fun pending(task: Task, repeats: Boolean, sent: Set<String>, now: Long, today: String): List<PlannedReminder> {
        if (task.isCompleted || task.date < today || task.time.isNullOrBlank()) return emptyList()
        return ReminderKind.entries.mapNotNull { kind ->
            val at = dueAt(task, kind) ?: return@mapNotNull null
            if ((kind.isRepeat && !repeats) || kind.name in sent || at <= now) null else PlannedReminder(kind, at)
        }
    }

    fun canDeliver(task: Task, kind: ReminderKind, repeats: Boolean, scheduledAt: Long,
                   now: Long, today: String): Boolean {
        if (task.isCompleted || task.date != today || task.time.isNullOrBlank() || (kind.isRepeat && !repeats)) return false
        val at = dueAt(task, kind) ?: return false
        if (at != scheduledAt || now < at) return false
        // An inexact delivery must not create a burst of obsolete reminders.
        val next = when (kind) {
            ReminderKind.BEFORE -> dueAt(task, ReminderKind.REPEAT_10)
            ReminderKind.REPEAT_10 -> dueAt(task, ReminderKind.REPEAT_30)
            ReminderKind.REPEAT_30 -> at + 20 * 60_000L
        } ?: return false
        return now < next
    }
}
