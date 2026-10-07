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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kunlikvazifalar.data.model.Task

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (text: String, time: String?) -> Unit,
    isSaving: Boolean = false
) {
    val context = LocalContext.current
    var taskText by remember { mutableStateOf(task.text) }
    var selectedTime by remember { mutableStateOf(task.time) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {
            Text(text = "Vazifani tahrirlash", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = {
                        taskText = it
                        if (it.isNotBlank()) hasError = false
                    },
                    label = { Text("Vazifa matni *") },
                    isError = hasError,
                    supportingText = if (hasError) {
                        { Text("Vazifa matni kiritilishi shart", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vaqt (ixtiyoriy):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (selectedTime == null) "Vaqt tanlash" else "O‘zgartirish")
                        }

                        if (selectedTime != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(onClick = { selectedTime = null }) {
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
                    val trimmed = taskText.trim()
                    if (trimmed.isBlank()) {
                        hasError = true
                    } else if (!isSaving) {
                        onConfirm(trimmed, selectedTime)
                    }
                },
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saqlanmoqda..." else "Saqlash")
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
