package com.example.kunlikvazifalar.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.model.TaskPriority

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "kunlik_vazifalar.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TEXT = "text"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_IS_COMPLETED = "is_completed"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_PRIORITY = "priority"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TEXT TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TIME TEXT,
                $COLUMN_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_PRIORITY INTEGER NOT NULL DEFAULT 1 CHECK(priority BETWEEN 0 AND 2)
            )
        """.trimIndent()
        db.execSQL(createTableSql)
        createReminderLedger(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN priority INTEGER NOT NULL DEFAULT 1 CHECK(priority BETWEEN 0 AND 2)")
            createReminderLedger(db)
        }
    }

    private fun createReminderLedger(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS reminder_deliveries (
            task_id INTEGER NOT NULL, task_date TEXT NOT NULL, task_time TEXT NOT NULL,
            kind TEXT NOT NULL, sent_at INTEGER NOT NULL,
            PRIMARY KEY(task_id, task_date, task_time, kind)
        )""")
    }

    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TEXT, task.text)
            put(COLUMN_DATE, task.date)
            put(COLUMN_TIME, task.time)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_CREATED_AT, task.createdAt)
            put(COLUMN_PRIORITY, task.priority.value)
        }
        return db.insertOrThrow(TABLE_TASKS, null, values)
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TEXT, task.text)
            put(COLUMN_DATE, task.date)
            put(COLUMN_TIME, task.time)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_PRIORITY, task.priority.value)
        }
        return db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    }

    fun setTaskCompleted(id: Long, isCompleted: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        return db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val rows = db.delete(TABLE_TASKS, "$COLUMN_ID = ?", arrayOf(id.toString()))
            db.delete("reminder_deliveries", "task_id = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
            rows
        } finally { db.endTransaction() }
    }

    fun getTaskById(id: Long): Task? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return parseTask(it)
            }
        }
        return null
    }

    /**
     * Berilgan sana (masalan, bugun) uchun vazifalar:
     * Vaqtlilar vaqt bo'yicha saralangan, vaqtsizlari ulardan keyin.
     */
    fun getTasksForDate(date: String): List<Task> {
        val db = readableDatabase
        val tasks = mutableListOf<Task>()
        val orderBy = "$COLUMN_PRIORITY DESC, CASE WHEN $COLUMN_TIME IS NULL OR $COLUMN_TIME = '' THEN 1 ELSE 0 END ASC, $COLUMN_TIME ASC, $COLUMN_ID ASC"
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(date),
            null,
            null,
            orderBy
        )
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(parseTask(it))
            }
        }
        return tasks
    }

    /**
     * Tarix vazifalari: bugundan oldingi barcha kunlar.
     * Sana bo'yicha kamayish tartibida (eng yangisi tepada).
     */
    fun getHistoryTasks(currentDate: String): List<Task> {
        val db = readableDatabase
        val tasks = mutableListOf<Task>()
        val orderBy = "$COLUMN_DATE DESC, $COLUMN_PRIORITY DESC, CASE WHEN $COLUMN_TIME IS NULL OR $COLUMN_TIME = '' THEN 1 ELSE 0 END ASC, $COLUMN_TIME ASC, $COLUMN_ID ASC"
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_DATE < ?",
            arrayOf(currentDate),
            null,
            null,
            orderBy
        )
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(parseTask(it))
            }
        }
        return tasks
    }

    /**
     * Telefon qayta yonganda (boot) kelgusi faol eslatmalarni qayta o'rnatish uchun.
     */
    fun getActiveFutureTimedTasks(currentDate: String): List<Task> {
        val db = readableDatabase
        val tasks = mutableListOf<Task>()
        val selection = "$COLUMN_IS_COMPLETED = 0 AND $COLUMN_TIME IS NOT NULL AND $COLUMN_TIME != '' AND $COLUMN_DATE >= ?"
        val cursor = db.query(
            TABLE_TASKS,
            null,
            selection,
            arrayOf(currentDate),
            null,
            null,
            "$COLUMN_DATE ASC, $COLUMN_TIME ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(parseTask(it))
            }
        }
        return tasks
    }

    private fun parseTask(cursor: Cursor): Task {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT))
        val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
        val time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
        val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1
        val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))

        return Task(
            id = id,
            text = text,
            date = date,
            time = if (time.isNullOrBlank()) null else time,
            isCompleted = isCompleted,
            createdAt = createdAt,
            priority = TaskPriority.fromValue(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)))
        )
    }

    fun getDeliveredKinds(task: Task): Set<String> = readableDatabase.query(
        "reminder_deliveries", arrayOf("kind"), "task_id = ? AND task_date = ? AND task_time = ?",
        arrayOf(task.id.toString(), task.date, task.time.orEmpty()), null, null, null
    ).use { cursor -> buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) } }

    /** Called under the reminder lock. The unique key survives process death/reboot. */
    fun claimDelivery(task: Task, kind: String, now: Long): Boolean {
        val values = ContentValues().apply {
            put("task_id", task.id); put("task_date", task.date); put("task_time", task.time)
            put("kind", kind); put("sent_at", now)
        }
        return writableDatabase.insertWithOnConflict("reminder_deliveries", null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
    }

    fun releaseDelivery(task: Task, kind: String) {
        writableDatabase.delete("reminder_deliveries", "task_id = ? AND task_date = ? AND task_time = ? AND kind = ?",
            arrayOf(task.id.toString(), task.date, task.time.orEmpty(), kind))
    }
}
