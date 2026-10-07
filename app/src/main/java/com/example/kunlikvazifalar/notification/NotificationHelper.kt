package com.example.kunlikvazifalar.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kunlikvazifalar.MainActivity
import com.example.kunlikvazifalar.R
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.util.DateUtils

enum class ReminderScheduleResult {
    SCHEDULED,
    NO_TIME,
    TOO_CLOSE_OR_PAST,
    PERMISSION_DENIED
}

object NotificationHelper {

    const val CHANNEL_ID = "kunlik_vazifalar_reminders"
    private const val CHANNEL_NAME = "Vazifalar eslatmalari"
    private const val CHANNEL_DESC = "Vazifalar boshlanishidan 5 daqiqa oldin eslatish"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Vazifa boshlanishidan 5 daqiqa oldin eslatma rejalashtirish.
     */
    fun scheduleReminder(context: Context, task: Task): ReminderScheduleResult {
        val time = task.time ?: return ReminderScheduleResult.NO_TIME
        val reminderMillis = DateUtils.calculateReminderTimeMillis(task.date, time)
            ?: return ReminderScheduleResult.NO_TIME

        val now = System.currentTimeMillis()
        if (reminderMillis <= now) {
            // 5 daqiqadan kam qolgan yoki o'tib ketgan
            return ReminderScheduleResult.TOO_CLOSE_OR_PAST
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return ReminderScheduleResult.PERMISSION_DENIED

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
            putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(AlarmReceiver.EXTRA_TASK_TEXT, task.text)
            putExtra(AlarmReceiver.EXTRA_TASK_TIME, task.time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderMillis,
                    pendingIntent
                )
            }
            return ReminderScheduleResult.SCHEDULED
        } catch (_: SecurityException) {
            // Agar ruxsat bo'lmasa inexact orqali urinib ko'rish
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderMillis,
                    pendingIntent
                )
                return ReminderScheduleResult.SCHEDULED
            } catch (_: Exception) {
                return ReminderScheduleResult.PERMISSION_DENIED
            }
        }
    }

    /**
     * Eslatmani bekor qilish (vazifa bajarilganda, tahrirlanganda yoki o'chirilganda).
     */
    fun cancelReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Bildirishnomani ekranga chiqarish.
     */
    fun showNotification(context: Context, taskId: Long, taskText: String) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messageText = "5 daqiqadan keyin «$taskText» ishini qilishingiz kerak."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Vazifa eslatmasi")
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(taskId.toInt(), builder.build())
        } catch (_: SecurityException) {
            // Ruxsat yo'q bo'lsa xato tashlamaslik
        }
    }
}
