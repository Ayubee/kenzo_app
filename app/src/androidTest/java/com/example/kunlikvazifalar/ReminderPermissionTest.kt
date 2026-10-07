package com.example.kunlikvazifalar

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.notification.NotificationHelper
import com.example.kunlikvazifalar.notification.ReminderScheduleResult
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Permission state is configured externally. No app data or permission is changed here. */
@RunWith(AndroidJUnit4::class)
class ReminderPermissionTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    // Reserved test identity: no DB row is inserted and existing task alarms are untouched.
    private val task = Task(id = Long.MAX_VALUE - 55, text = "Permission regression", date = "2099-01-01", time = "12:00")

    @After
    fun removeOnlyTestAlarm() {
        NotificationHelper.cancelReminder(context, task.id)
    }

    @Test
    fun notificationsDeniedDoesNotScheduleOrPost() {
        assumeFalse(NotificationHelper.areNotificationsEnabled(context))
        assertEquals(ReminderScheduleResult.PERMISSION_DENIED, NotificationHelper.scheduleReminder(context, task))
        assertFalse(NotificationHelper.showNotification(context, task.id, task.text, task.time))
    }

    @Test
    fun exactAccessDeniedUsesApproximateResult() {
        assumeTrue(NotificationHelper.areNotificationsEnabled(context))
        assumeFalse(NotificationHelper.canScheduleExactAlarms(context))
        assertEquals(ReminderScheduleResult.SCHEDULED_APPROXIMATE, NotificationHelper.scheduleReminder(context, task))
    }

    @Test
    fun exactAccessGrantedUsesExactResult() {
        assumeTrue(NotificationHelper.areNotificationsEnabled(context))
        assumeTrue(NotificationHelper.canScheduleExactAlarms(context))
        assertEquals(ReminderScheduleResult.SCHEDULED, NotificationHelper.scheduleReminder(context, task))
    }

    @Test
    fun completedUntimedAndPastTasksAreNotScheduled() {
        assertEquals(ReminderScheduleResult.NO_TIME, NotificationHelper.scheduleReminder(context, task.copy(isCompleted = true)))
        assertEquals(ReminderScheduleResult.NO_TIME, NotificationHelper.scheduleReminder(context, task.copy(time = null)))
        assertEquals(ReminderScheduleResult.TOO_CLOSE_OR_PAST, NotificationHelper.scheduleReminder(context, task.copy(date = "2020-01-01")))
    }
}
