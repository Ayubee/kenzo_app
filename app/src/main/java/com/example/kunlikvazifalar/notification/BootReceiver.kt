package com.example.kunlikvazifalar.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.concurrent.thread

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val supported = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        )
        if (intent.action !in supported) return
        if (intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED &&
            !NotificationHelper.canScheduleExactAlarms(context)
        ) return
        val pending = goAsync()
        thread(name = "kenzo-reschedule") {
            try {
                NotificationHelper.rescheduleReminders(context.applicationContext)
            } catch (error: RuntimeException) {
                Log.e("KenzoReminders", "Could not restore reminders", error)
            } finally {
                pending.finish()
            }
        }
    }
}
