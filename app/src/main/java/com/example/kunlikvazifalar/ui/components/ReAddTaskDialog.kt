package com.example.kunlikvazifalar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.model.TaskPriority

@Composable
fun ReAddTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (Task, String?, TaskPriority) -> Unit,
    isSaving: Boolean = false
) {
    val context = LocalContext.current
    var selectedTime by rememberSaveable(task.id) { mutableStateOf<String?>(task.time) }
    var priority by rememberSaveable(task.id) { mutableStateOf(task.priority) }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {
            Text(text = "Bugunga qayta qo‘shish", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(
                    text = "Vazifa:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = task.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                PrioritySelector(priority, !isSaving) { priority = it }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Bugungi vaqt (ixtiyoriy):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (selectedTime != null) "🕒 $selectedTime" else "Vaqt belgilanmagan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )

                    Row {
                        OutlinedButton(
                            onClick = {
                                TimePickerHelper.showTimePicker(context, selectedTime) { time ->
                                    selectedTime = time
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isSaving
                        ) {
                            Text(if (selectedTime == null) "Vaqt tanlash" else "O‘zgartirish")
                        }

                        if (selectedTime != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(onClick = { selectedTime = null }, enabled = !isSaving) {
                                Text("Tozalash", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isSaving) {
                        onConfirm(task, selectedTime, priority)
                    }
                },
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Qo‘shilmoqda..." else "Bugunga qo‘shish")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Bekor qilish")
            }
        }
    )
}
