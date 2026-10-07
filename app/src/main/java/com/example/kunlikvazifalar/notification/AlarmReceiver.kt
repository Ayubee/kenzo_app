package com.example.kunlikvazifalar.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMINDER = "com.example.kunlikvazifalar.ACTION_TASK_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TEXT = "extra_task_text"
        const val EXTRA_TASK_TIME = "extra_task_time"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskText = intent.getStringExtra(EXTRA_TASK_TEXT) ?: return

        if (taskId != -1L) {
            // Ma'lumotlar bazasidan tekshiramiz: vazifa mavjudmi va bajarilmaganmi
            val dbHelper = TaskDatabaseHelper(context)
            val task = dbHelper.getTaskById(taskId)
            if (task != null && !task.isCompleted) {
                NotificationHelper.showNotification(context, taskId, taskText)
            }
        }
    }
}
