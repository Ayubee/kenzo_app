package com.example.kunlikvazifalar.data.repository

import android.content.Context
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.model.TaskPriority
import com.example.kunlikvazifalar.notification.NotificationHelper
import com.example.kunlikvazifalar.notification.ReminderScheduleResult
import com.example.kunlikvazifalar.util.DateUtils

class TaskRepository(private val context: Context) {
    private val dbHelper = TaskDatabaseHelper(context)
    init { NotificationHelper.createNotificationChannel(context) }
    fun getTodayTasks(todayDate: String) = dbHelper.getTasksForDate(todayDate)
    fun getHistoryTasks(currentDate: String) = dbHelper.getHistoryTasks(currentDate)
    fun getTask(id: Long) = dbHelper.getTaskById(id)

    fun addTask(text: String, date: String, time: String?, priority: TaskPriority = TaskPriority.NORMAL): Pair<Task, ReminderScheduleResult> = synchronized(NotificationHelper.reminderLock) {
        val draft = Task(text = text.trim(), date = date, time = time?.takeIf { it.isNotBlank() }, priority = priority)
        val task = draft.copy(id = dbHelper.insertTask(draft))
        task to NotificationHelper.scheduleReminder(context, task)
    }

    fun updateTask(task: Task): Pair<Boolean, ReminderScheduleResult> = synchronized(NotificationHelper.reminderLock) {
        val current = dbHelper.getTaskById(task.id) ?: return@synchronized false to ReminderScheduleResult.NO_TIME
        // Editing text/time/priority must not undo a notification action performed meanwhile.
        val edited = task.copy(isCompleted = current.isCompleted)
        val success = dbHelper.updateTask(edited) > 0
        if (success) {
            NotificationHelper.cancelReminder(context, task.id)
            true to NotificationHelper.scheduleReminder(context, edited)
        } else false to ReminderScheduleResult.NO_TIME
    }

    fun toggleTaskCompletion(task: Task): Boolean = synchronized(NotificationHelper.reminderLock) {
        // Read current storage, not a potentially stale card captured before another action.
        val current = dbHelper.getTaskById(task.id) ?: return@synchronized false
        val updated = current.copy(isCompleted = !current.isCompleted)
        val success = dbHelper.setTaskCompleted(task.id, updated.isCompleted) > 0
        if (success) {
            NotificationHelper.cancelReminder(context, task.id)
            if (!updated.isCompleted) NotificationHelper.scheduleReminder(context, updated)
        }
        success
    }

    fun deleteTask(taskId: Long): Boolean = synchronized(NotificationHelper.reminderLock) {
        val success = dbHelper.deleteTask(taskId) > 0
        if (success) NotificationHelper.cancelReminder(context, taskId)
        success
    }

    fun reAddHistoryTaskToToday(originalTask: Task, newTime: String?, priority: TaskPriority = originalTask.priority) =
        addTask(originalTask.text, DateUtils.getTodayDate(), newTime, priority)
}
