package com.example.kunlikvazifalar.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.concurrent.thread

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_REMINDER = "com.example.kunlikvazifalar.ACTION_TASK_REMINDER"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TIME = "extra_task_time"
        const val EXTRA_REMINDER_AT = "extra_reminder_at"
        const val EXTRA_KIND = "extra_reminder_kind"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId < 0) return
        val kind = ReminderKind.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_KIND) } ?: return
        val scheduledAt = intent.getLongExtra(EXTRA_REMINDER_AT, -1)
        val pending = goAsync()
        thread(name = "kenzo-deliver") {
            try { NotificationHelper.deliver(context.applicationContext, taskId, kind, scheduledAt) }
            catch (error: RuntimeException) { Log.w("KenzoReminders", "Delivery failed", error) }
            finally { pending.finish() }
        }
    }
}
