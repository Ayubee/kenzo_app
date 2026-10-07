package com.example.kunlikvazifalar.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper
import com.example.kunlikvazifalar.util.DateUtils

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val dbHelper = TaskDatabaseHelper(context)
            val today = DateUtils.getTodayDate()
            val activeTasks = dbHelper.getActiveFutureTimedTasks(today)

            NotificationHelper.createNotificationChannel(context)

            for (task in activeTasks) {
                NotificationHelper.scheduleReminder(context, task)
            }
        }
    }
}
