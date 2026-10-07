package com.example.kunlikvazifalar.data.repository

import android.content.Context
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.notification.NotificationHelper
import com.example.kunlikvazifalar.notification.ReminderScheduleResult
import com.example.kunlikvazifalar.util.DateUtils

class TaskRepository(private val context: Context) {

    private val dbHelper = TaskDatabaseHelper(context)

    init {
        NotificationHelper.createNotificationChannel(context)
    }

    fun getTodayTasks(todayDate: String): List<Task> {
        return dbHelper.getTasksForDate(todayDate)
    }

    fun getHistoryTasks(currentDate: String): List<Task> {
        return dbHelper.getHistoryTasks(currentDate)
    }

    fun addTask(text: String, date: String, time: String?): Pair<Task, ReminderScheduleResult> {
        val newTask = Task(
            text = text.trim(),
            date = date,
            time = if (time.isNullOrBlank()) null else time,
            isCompleted = false
        )
        val insertedId = dbHelper.insertTask(newTask)
        val createdTask = newTask.copy(id = insertedId)

        var reminderResult = ReminderScheduleResult.NO_TIME
        if (createdTask.time != null) {
            reminderResult = NotificationHelper.scheduleReminder(context, createdTask)
        }

        return Pair(createdTask, reminderResult)
    }

    fun updateTask(task: Task): Pair<Boolean, ReminderScheduleResult> {
        val rows = dbHelper.updateTask(task)
        val success = rows > 0

        NotificationHelper.cancelReminder(context, task.id)

        var reminderResult = ReminderScheduleResult.NO_TIME
        if (success && !task.isCompleted && task.time != null) {
            reminderResult = NotificationHelper.scheduleReminder(context, task)
        }

        return Pair(success, reminderResult)
    }

    fun toggleTaskCompletion(task: Task): Boolean {
        val newStatus = !task.isCompleted
        val rows = dbHelper.setTaskCompleted(task.id, newStatus)
        val success = rows > 0

        if (success) {
            if (newStatus) {
                // Bajarildi deb belgilansa, eslatmani bekor qilamiz
                NotificationHelper.cancelReminder(context, task.id)
            } else {
                // Qayta faol holatga keltirilsa va vaqti bo'lsa, eslatmani qayta tekshiramiz
                if (task.time != null && task.date >= DateUtils.getTodayDate()) {
                    NotificationHelper.scheduleReminder(context, task.copy(isCompleted = false))
                }
            }
        }
        return success
    }

    fun deleteTask(taskId: Long): Boolean {
        NotificationHelper.cancelReminder(context, taskId)
        val rows = dbHelper.deleteTask(taskId)
        return rows > 0
    }

    fun reAddHistoryTaskToToday(originalTask: Task, newTime: String?): Pair<Task, ReminderScheduleResult> {
        val today = DateUtils.getTodayDate()
        // Eski yozuv tarixda saqlanadi, bugun uchun yangi bajarilmagan nusxa yaratiladi
        return addTask(
            text = originalTask.text,
            date = today,
            time = newTime
        )
    }
}
