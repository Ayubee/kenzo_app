package com.example.kunlikvazifalar.ui.components

import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar

object TimePickerHelper {
    fun showTimePicker(
        context: Context,
        initialTime: String? = null,
        onTimeSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        if (!initialTime.isNullOrBlank()) {
            val parts = initialTime.split(":")
            if (parts.size == 2) {
                hour = parts[0].toIntOrNull() ?: hour
                minute = parts[1].toIntOrNull() ?: minute
            }
        }

        val dialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val formatted = String.format(java.util.Locale.US, "%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(formatted)
            },
            hour,
            minute,
            true // 24 soatlik format
        )
        dialog.show()
    }
}
