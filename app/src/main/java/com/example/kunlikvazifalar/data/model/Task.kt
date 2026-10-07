package com.example.kunlikvazifalar.data.model

data class Task(
    val id: Long = 0,
    val text: String,
    val date: String, // Format: "yyyy-MM-dd" masalan: "2026-10-07"
    val time: String? = null, // Format: "HH:mm" masalan: "14:30" yoki null
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
