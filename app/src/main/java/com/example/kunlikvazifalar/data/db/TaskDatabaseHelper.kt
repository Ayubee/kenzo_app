package com.example.kunlikvazifalar.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kunlikvazifalar.data.model.Task

class TaskDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "kunlik_vazifalar.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TEXT = "text"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_IS_COMPLETED = "is_completed"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TEXT TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_TIME TEXT,
                $COLUMN_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Kelgusidagi migratsiyalar uchun
    }

    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TEXT, task.text)
            put(COLUMN_DATE, task.date)
            put(COLUMN_TIME, task.time)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_CREATED_AT, task.createdAt)
        }
        return db.insert(TABLE_TASKS, null, values)
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TEXT, task.text)
            put(COLUMN_DATE, task.date)
            put(COLUMN_TIME, task.time)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
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
        return db.delete(TABLE_TASKS, "$COLUMN_ID = ?", arrayOf(id.toString()))
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
        val orderBy = "CASE WHEN $COLUMN_TIME IS NULL OR $COLUMN_TIME = '' THEN 1 ELSE 0 END ASC, $COLUMN_TIME ASC, $COLUMN_ID ASC"
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
        val orderBy = "$COLUMN_DATE DESC, CASE WHEN $COLUMN_TIME IS NULL OR $COLUMN_TIME = '' THEN 1 ELSE 0 END ASC, $COLUMN_TIME ASC, $COLUMN_ID ASC"
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
            createdAt = createdAt
        )
    }
}
