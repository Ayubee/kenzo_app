package com.example.kunlikvazifalar.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper
import kotlin.concurrent.thread

/** Explicit immutable PendingIntent; no exported endpoint for external task mutations. */
class CompleteTaskReceiver : BroadcastReceiver() {
    companion object { const val ACTION_COMPLETE = "com.example.kunlikvazifalar.COMPLETE_TASK" }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_COMPLETE) return
        val id = intent.getLongExtra(AlarmReceiver.EXTRA_TASK_ID, -1)
        if (id < 0) return
        val pending = goAsync()
        thread(name = "kenzo-complete") {
            try {
                synchronized(NotificationHelper.reminderLock) {
                    TaskDatabaseHelper(context).use { db ->
                        if (db.setTaskCompleted(id, true) > 0) NotificationHelper.cancelReminder(context, id)
                    }
                }
            } catch (error: RuntimeException) { Log.w("KenzoReminders", "Completion failed", error) }
            finally { pending.finish() }
        }
    }
}
