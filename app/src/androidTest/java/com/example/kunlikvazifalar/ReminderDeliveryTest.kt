package com.example.kunlikvazifalar

import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.preferences.UserPreferences
import com.example.kunlikvazifalar.notification.*
import com.example.kunlikvazifalar.util.DateUtils
import org.junit.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/** Real SQLite and Android notification service; isolated storage and reserved identities. */
@RunWith(AndroidJUnit4::class)
class ReminderDeliveryTest {
    private lateinit var context: IsolatedContext
    private lateinit var db: TaskDatabaseHelper
    private lateinit var task: Task
    private val id = Long.MAX_VALUE - 100
    private val kind = ReminderKind.REPEAT_10

    @Before fun setup() {
        context = IsolatedContext(ApplicationProvider.getApplicationContext())
        db = TaskDatabaseHelper(context)
        assumeTrue(NotificationHelper.areNotificationsEnabled(context))
        val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, -11) }
        task = Task(id = id, text = "Reminder delivery regression", date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time),
            time = DateUtils.formatTime24(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
        assumeTrue(task.date == DateUtils.getTodayDate())
        db.writableDatabase.execSQL("INSERT INTO tasks(id,text,date,time,is_completed,created_at,priority) VALUES(?,?,?,?,0,?,1)",
            arrayOf<Any?>(id, task.text, task.date, task.time, task.createdAt))
    }
    @After fun cleanup() {
        if (::context.isInitialized) {
            NotificationHelper.cancelReminder(context, id)
            if (::db.isInitialized) db.close()
            context.deleteDatabase("kunlik_vazifalar.db")
            context.deleteSharedPreferences("kunlik_vazifalar_prefs")
        }
    }
    private fun deliver(at: Long = ReminderPolicy.dueAt(task, kind)!!) = NotificationHelper.deliver(context, id, kind, at)

    @Test fun oneDeliveryPersistsAcrossReopenAndNotificationDismissal() {
        assertTrue(deliver())
        val notification = context.getSystemService(NotificationManager::class.java).activeNotifications.single { it.tag == id.toString() }
        assertTrue(notification.notification.extras.getCharSequence("android.text").toString().contains("vazifasining vaqti o‘tdi"))
        assertEquals("Bajarildi", notification.notification.actions.single().title.toString())
        // Dismissing/opening a notification must not complete its task or clear the ledger.
        context.getSystemService(NotificationManager::class.java).cancel(id.toString(), 0)
        db.close(); db = TaskDatabaseHelper(context)
        assertFalse(db.getTaskById(id)!!.isCompleted)
        assertFalse(deliver())
        assertEquals(setOf("REPEAT_10"), db.getDeliveredKinds(task))
        NotificationHelper.scheduleReminder(context, task)
        assertFalse(deliver())
    }
    @Test fun disabledCompletedEditedAndDeletedTasksCannotDeliver() {
        UserPreferences(context).setRepeatsEnabled(false)
        assertFalse(deliver())
        UserPreferences(context).setRepeatsEnabled(true)
        db.setTaskCompleted(id, true)
        assertFalse(deliver())
        db.updateTask(task.copy(time = "23:59"))
        assertFalse(deliver())
        db.deleteTask(id)
        assertFalse(deliver())
    }
    @Test fun deliveryUsesCurrentTextAndRejectsEarlyOrObsoleteSlot() {
        val due = ReminderPolicy.dueAt(task, kind)!!
        assertFalse(deliver(due + 60_000)) // Cannot accept an edited alarm identity.
        db.updateTask(task.copy(text = "Latest task text"))
        assertTrue(deliver())
        val notification = context.getSystemService(NotificationManager::class.java).activeNotifications.single { it.tag == id.toString() }
        assertTrue(notification.notification.extras.getCharSequence("android.text").toString().contains("Latest task text"))
    }

    private class IsolatedContext(base: Context) : ContextWrapper(base) {
        private val prefix = "kenzo_delivery_${UUID.randomUUID()}_"
        override fun getDatabasePath(name: String): File = baseContext.getDatabasePath(prefix + name)
        override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase = baseContext.openOrCreateDatabase(prefix + name, mode, factory)
        override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?): SQLiteDatabase = baseContext.openOrCreateDatabase(prefix + name, mode, factory, errorHandler)
        override fun deleteDatabase(name: String) = baseContext.deleteDatabase(prefix + name)
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = baseContext.getSharedPreferences(prefix + name, mode)
        override fun deleteSharedPreferences(name: String) = baseContext.deleteSharedPreferences(prefix + name)
    }
}
