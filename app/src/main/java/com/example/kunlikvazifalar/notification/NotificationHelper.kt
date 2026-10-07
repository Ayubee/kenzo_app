package com.example.kunlikvazifalar.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.kunlikvazifalar.MainActivity
import com.example.kunlikvazifalar.R
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.preferences.UserPreferences
import com.example.kunlikvazifalar.util.DateUtils

enum class ReminderScheduleResult {
    SCHEDULED, SCHEDULED_APPROXIMATE, NO_TIME, TOO_CLOSE_OR_PAST, PERMISSION_DENIED, SCHEDULING_FAILED
}

object NotificationHelper {
    const val CHANNEL_ID = "kunlik_vazifalar_reminders"
    const val EXTRA_OPEN_TASK_ID = "open_task_id"
    // All writes and deliveries share this lock; completion cannot race a late notification.
    val reminderLock = Any()

    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, "Vazifalar eslatmalari", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "5 daqiqa oldingi va ixtiyoriy takroriy eslatmalar"
                    enableVibration(true)
                }
                context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
            }
        } catch (error: RuntimeException) { Log.w("KenzoReminders", "Could not create channel", error) }
    }

    fun areNotificationsEnabled(context: Context): Boolean = try {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) false
        else if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) false
        else if (Build.VERSION.SDK_INT >= 26) context.getSystemService(NotificationManager::class.java)?.getNotificationChannel(CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
        else true
    } catch (_: RuntimeException) { false }

    fun canScheduleExactAlarms(context: Context): Boolean = try {
        val manager = context.getSystemService(AlarmManager::class.java)
        manager != null && (Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms())
    } catch (_: RuntimeException) { false }

    fun scheduleReminder(context: Context, task: Task): ReminderScheduleResult = synchronized(reminderLock) {
        try {
            // Reconcile this identity; set() replaces the same PendingIntent, never adds duplicates.
            cancelAlarms(context, task.id)
            if (task.isCompleted || task.time.isNullOrBlank()) return@synchronized ReminderScheduleResult.NO_TIME
            if (task.date < DateUtils.getTodayDate()) return@synchronized ReminderScheduleResult.TOO_CLOSE_OR_PAST
            createNotificationChannel(context)
            if (!areNotificationsEnabled(context)) return@synchronized ReminderScheduleResult.PERMISSION_DENIED
            val sent = TaskDatabaseHelper(context).use { it.getDeliveredKinds(task) }
            val plans = ReminderPolicy.pending(task, UserPreferences(context).repeatsEnabled(), sent,
                System.currentTimeMillis(), DateUtils.getTodayDate())
            if (plans.isEmpty()) return@synchronized ReminderScheduleResult.TOO_CLOSE_OR_PAST
            val manager = context.getSystemService(AlarmManager::class.java) ?: return@synchronized ReminderScheduleResult.SCHEDULING_FAILED
            var approximate = false
            for (plan in plans) {
                val intent = reminderIntent(context, task.id, plan.kind).apply {
                    putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
                    putExtra(AlarmReceiver.EXTRA_REMINDER_AT, plan.at)
                    putExtra(AlarmReceiver.EXTRA_KIND, plan.kind.name)
                }
                val pending = PendingIntent.getBroadcast(context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (canScheduleExactAlarms(context)) {
                    try {
                        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plan.at, pending)
                        continue
                    } catch (_: SecurityException) { /* Permission may have changed. */ }
                }
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plan.at, pending)
                approximate = true
            }
            if (approximate) ReminderScheduleResult.SCHEDULED_APPROXIMATE else ReminderScheduleResult.SCHEDULED
        } catch (error: RuntimeException) {
            Log.w("KenzoReminders", "Could not schedule reminders", error)
            // Never report success after a partially failed plan.
            try { cancelAlarms(context, task.id) } catch (_: RuntimeException) { }
            ReminderScheduleResult.SCHEDULING_FAILED
        }
    }

    fun cancelReminder(context: Context, taskId: Long) = synchronized(reminderLock) {
        try {
            cancelAlarms(context, taskId)
            NotificationManagerCompat.from(context).cancel(taskId.toString(), 0)
            NotificationManagerCompat.from(context).cancel(taskId.toInt()) // Previous app's notification identity.
        } catch (error: RuntimeException) { Log.w("KenzoReminders", "Could not cancel reminders", error) }
    }

    private fun cancelAlarms(context: Context, taskId: Long) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val intents = ReminderKind.entries.map { reminderIntent(context, taskId, it) } +
            Intent(context, AlarmReceiver::class.java).setAction(AlarmReceiver.ACTION_REMINDER) // Legacy -5 alarm.
        for (intent in intents) {
            val pending = PendingIntent.getBroadcast(context, taskId.toInt(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (pending != null) { manager.cancel(pending); pending.cancel() }
        }
    }

    /** Reconcile all rows, including historical/completed rows whose old alarms must disappear. */
    fun rescheduleReminders(context: Context) = synchronized(reminderLock) {
        createNotificationChannel(context)
        val today = DateUtils.getTodayDate()
        val tasks = TaskDatabaseHelper(context).use { it.getTasksForDate(today) + it.getHistoryTasks(today) + it.getActiveFutureTimedTasks(today).filter { t -> t.date > today } }
        for (task in tasks) {
            if (task.isCompleted || task.date < today || task.time.isNullOrBlank()) cancelReminder(context, task.id)
            else scheduleReminder(context, task)
        }
    }

    /** Called on an IO thread. Claiming before posting ensures at most one delivery per slot. */
    fun deliver(context: Context, taskId: Long, kind: ReminderKind, scheduledAt: Long): Boolean = synchronized(reminderLock) {
        try {
            TaskDatabaseHelper(context).use { db ->
                val task = db.getTaskById(taskId) ?: return@synchronized false
                val now = System.currentTimeMillis()
                if (!ReminderPolicy.canDeliver(task, kind, UserPreferences(context).repeatsEnabled(), scheduledAt, now, DateUtils.getTodayDate())) return@synchronized false
                if (!areNotificationsEnabled(context) || !db.claimDelivery(task, kind.name, now)) return@synchronized false
                val shown = showNotification(context, task.id, task.text, task.time, kind)
                if (!shown) db.releaseDelivery(task, kind.name)
                shown
            }
        } catch (error: RuntimeException) { Log.w("KenzoReminders", "Could not deliver reminder", error); false }
    }

    fun showNotification(context: Context, taskId: Long, taskText: String, taskTime: String? = null, kind: ReminderKind = ReminderKind.BEFORE): Boolean = try {
        createNotificationChannel(context)
        if (!areNotificationsEnabled(context)) false else {
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                data = Uri.parse("kenzo://task/$taskId")
                putExtra(EXTRA_OPEN_TASK_ID, taskId)
            }
            val open = PendingIntent.getActivity(context, taskId.toInt(), openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val doneIntent = Intent(context, CompleteTaskReceiver::class.java).apply {
                action = CompleteTaskReceiver.ACTION_COMPLETE
                data = Uri.parse("kenzo://complete/$taskId")
                putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            }
            val done = PendingIntent.getBroadcast(context, taskId.toInt(), doneIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val message = if (kind.isRepeat) "«$taskText» vazifasining vaqti o‘tdi. Bajardingizmi?"
                else if (taskTime == null) taskText else "$taskText · Belgilangan vaqt: $taskTime"
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Kenzo App · Vazifa eslatmasi")
                .setContentText(message).setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
                .setContentIntent(open).setOnlyAlertOnce(false)
                .addAction(R.drawable.ic_notification, "Bajarildi", done).build()
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) false
            else { NotificationManagerCompat.from(context).notify(taskId.toString(), 0, notification); true }
        }
    } catch (error: RuntimeException) { Log.w("KenzoReminders", "Could not post notification", error); false }

    private fun reminderIntent(context: Context, taskId: Long, kind: ReminderKind) = Intent(context, AlarmReceiver::class.java).apply {
        action = AlarmReceiver.ACTION_REMINDER
        data = Uri.parse("kenzo://reminder/$taskId/${kind.name}")
    }
}
