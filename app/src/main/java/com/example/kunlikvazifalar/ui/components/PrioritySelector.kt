package com.example.kunlikvazifalar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.kunlikvazifalar.data.model.TaskPriority

@Composable
fun PrioritySelector(priority: TaskPriority, enabled: Boolean, onChange: (TaskPriority) -> Unit) {
    Column {
        Text("Prioritet", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TaskPriority.entries.forEach { option ->
                FilterChip(selected = priority == option, enabled = enabled, onClick = { onChange(option) },
                    label = { Text(option.label) })
            }
        }
    }
}
