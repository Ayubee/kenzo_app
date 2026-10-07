package com.example.kunlikvazifalar.data.model

enum class TaskPriority(val value: Int, val label: String) {
    LOW(0, "Muhim emas"), NORMAL(1, "Muhim"), HIGH(2, "Juda muhim");

    companion object {
        fun fromValue(value: Int) = entries.firstOrNull { it.value == value } ?: NORMAL
    }
}

data class Task(
    val id: Long = 0,
    val text: String,
    val date: String, // Format: "yyyy-MM-dd" masalan: "2026-10-07"
    val time: String? = null, // Format: "HH:mm" masalan: "14:30" yoki null
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: TaskPriority = TaskPriority.NORMAL
)
