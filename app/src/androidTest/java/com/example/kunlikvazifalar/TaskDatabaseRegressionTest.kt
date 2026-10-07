package com.example.kunlikvazifalar

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.model.TaskPriority
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** SQLite regression tests use an isolated test database, never the user's database. */
@RunWith(AndroidJUnit4::class)
class TaskDatabaseRegressionTest {
    private lateinit var context: IsolatedDatabaseContext
    private lateinit var db: TaskDatabaseHelper

    @Before
    fun setup() {
        context = IsolatedDatabaseContext(ApplicationProvider.getApplicationContext())
        db = TaskDatabaseHelper(context)
    }

    @After
    fun cleanup() {
        db.close()
        context.deleteDatabase("kunlik_vazifalar.db")
    }

    @Test
    fun insertEditCompleteAndDeletePersistAcrossReopen() {
        val original = Task(text = "Saqlanadigan vazifa", date = "2026-10-07", time = "09:00")
        val id = db.insertTask(original)
        assertTrue(id > 0)
        assertEquals(1, db.updateTask(original.copy(id = id, text = "Tahrirlangan", time = "11:00")))
        assertEquals(1, db.setTaskCompleted(id, true))
        db.close()
        db = TaskDatabaseHelper(context)
        val restored = db.getTaskById(id)!!
        assertEquals("Tahrirlangan", restored.text)
        assertEquals("11:00", restored.time)
        assertTrue(restored.isCompleted)
        assertEquals(1, db.deleteTask(id))
        assertNull(db.getTaskById(id))
    }

    @Test
    fun realSqlSortsTimeAndKeepsHistorySeparate() {
        db.insertTask(Task(text = "Vaqtsiz", date = "2026-10-07"))
        db.insertTask(Task(text = "Kechroq", date = "2026-10-07", time = "13:00"))
        db.insertTask(Task(text = "Oldinroq", date = "2026-10-07", time = "07:00"))
        db.insertTask(Task(text = "Tarix", date = "2026-10-06"))
        assertEquals(listOf("Oldinroq", "Kechroq", "Vaqtsiz"), db.getTasksForDate("2026-10-07").map { it.text })
        assertEquals(listOf("Tarix"), db.getHistoryTasks("2026-10-07").map { it.text })
    }

    @Test
    fun reminderRecoveryIncludesOnlyActiveFutureTimedTasks() {
        db.insertTask(Task(text = "Kelajak", date = "2026-10-08", time = "10:00"))
        db.insertTask(Task(text = "Bugun", date = "2026-10-07", time = "11:00"))
        db.insertTask(Task(text = "Bajarilgan", date = "2026-10-07", time = "09:00", isCompleted = true))
        db.insertTask(Task(text = "Vaqtsiz", date = "2026-10-07"))
        db.insertTask(Task(text = "Oldingi", date = "2026-10-06", time = "10:00"))
        val recovered = db.getActiveFutureTimedTasks("2026-10-07")
        assertEquals(listOf("Bugun", "Kelajak"), recovered.map { it.text })
        assertFalse(recovered.any { it.isCompleted })
    }

    @Test
    fun migratesActualVersionOneWithoutLosingRows() {
        db.close()
        context.deleteDatabase("kunlik_vazifalar.db")
        context.openOrCreateDatabase("kunlik_vazifalar.db", Context.MODE_PRIVATE, null).use { old ->
            old.execSQL("CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, text TEXT NOT NULL, date TEXT NOT NULL, time TEXT, is_completed INTEGER NOT NULL DEFAULT 0, created_at INTEGER NOT NULL)")
            old.execSQL("INSERT INTO tasks VALUES(42, 'Original', '2026-10-07', '09:00', 1, 123456)")
            old.version = 1
        }
        db = TaskDatabaseHelper(context)
        val original = db.getTaskById(42)!!
        assertEquals("Original", original.text)
        assertTrue(original.isCompleted)
        assertEquals(123456L, original.createdAt)
        assertEquals("09:00", original.time)
        assertEquals(TaskPriority.NORMAL, original.priority)
        assertEquals(2, db.readableDatabase.version)
        assertTrue(db.getDeliveredKinds(original).isEmpty())
    }

    @Test
    fun priorityPrecedesTimeAndSurvivesReopen() {
        val date = "2026-10-07"
        db.insertTask(Task(text = "Low early", date = date, time = "07:00", priority = TaskPriority.LOW))
        val id = db.insertTask(Task(text = "High untimed", date = date, priority = TaskPriority.HIGH))
        db.insertTask(Task(text = "Normal", date = date, time = "08:00"))
        db.insertTask(Task(text = "High late", date = date, time = "18:00", priority = TaskPriority.HIGH))
        db.close(); db = TaskDatabaseHelper(context)
        assertEquals(listOf("High late", "High untimed", "Normal", "Low early"), db.getTasksForDate(date).map { it.text })
        val edited = db.getTaskById(id)!!.copy(priority = TaskPriority.LOW)
        db.updateTask(edited)
        assertEquals(TaskPriority.LOW, db.getTaskById(id)!!.priority)
    }

    @Test
    fun reminderLedgerSurvivesReopenAndIsUniquePerTimeAndSlot() {
        val original = Task(text = "Reminder", date = "2026-10-07", time = "12:00")
        val task = original.copy(id = db.insertTask(original))
        assertTrue(db.claimDelivery(task, "REPEAT_10", 123))
        assertFalse(db.claimDelivery(task, "REPEAT_10", 456))
        db.close(); db = TaskDatabaseHelper(context)
        assertEquals(setOf("REPEAT_10"), db.getDeliveredKinds(task))
        assertTrue(db.getDeliveredKinds(task.copy(time = "13:00")).isEmpty())
        assertTrue(db.claimDelivery(task, "REPEAT_30", 789))
        db.deleteTask(task.id)
        assertTrue(db.getDeliveredKinds(task).isEmpty())
    }

    private class IsolatedDatabaseContext(base: Context) : ContextWrapper(base) {
        private val prefix = "kenzo_test_${UUID.randomUUID()}_"

        override fun getDatabasePath(name: String): File = baseContext.getDatabasePath(prefix + name)

        override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase =
            baseContext.openOrCreateDatabase(prefix + name, mode, factory)

        override fun openOrCreateDatabase(
            name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?
        ): SQLiteDatabase = baseContext.openOrCreateDatabase(prefix + name, mode, factory, errorHandler)

        override fun deleteDatabase(name: String): Boolean = baseContext.deleteDatabase(prefix + name)
    }
}
