package com.example.kunlikvazifalar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.model.TaskPriority

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (text: String, time: String?, priority: TaskPriority) -> Unit,
    isSaving: Boolean = false
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var taskText by rememberSaveable(task.id) { mutableStateOf(task.text) }
    var selectedTime by rememberSaveable(task.id) { mutableStateOf(task.time) }
    var hasError by rememberSaveable(task.id) { mutableStateOf(false) }
    var priority by rememberSaveable(task.id) { mutableStateOf(task.priority) }
    var showTimePickerSheet by rememberSaveable { mutableStateOf(false) }

    if (showTimePickerSheet) {
        KenzoTimePickerBottomSheet(
            initialTime = selectedTime,
            onSave = { time ->
                selectedTime = time
                showTimePickerSheet = false
            },
            onCancel = {
                showTimePickerSheet = false
            },
            onClear = {
                selectedTime = null
                showTimePickerSheet = false
            }
        )
    }

    AlertDialog(
        modifier = Modifier.imePadding(),
        properties = DialogProperties(decorFitsSystemWindows = false),
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {
            Text(text = "Vazifani tahrirlash", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = {
                        taskText = it
                        if (it.isNotBlank()) hasError = false
                    },
                    label = { Text("Vazifa matni *") },
                    isError = hasError,
                    enabled = !isSaving,
                    supportingText = if (hasError) {
                        { Text("Vazifa matni kiritilishi shart", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                PrioritySelector(priority, !isSaving) { priority = it }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Vaqt (ixtiyoriy):",
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
                                focusManager.clearFocus()
                                showTimePickerSheet = true
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
                    val trimmed = taskText.trim()
                    if (trimmed.isBlank()) {
                        hasError = true
                    } else if (!isSaving) {
                        onConfirm(trimmed, selectedTime, priority)
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
