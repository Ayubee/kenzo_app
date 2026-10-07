package com.example.kunlikvazifalar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.ui.components.TaskCard

@Composable
fun TodayTasksScreen(
    activeTasks: List<Task>,
    completedTasks: List<Task>,
    completedExpanded: Boolean,
    onToggleCompletedExpanded: () -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTasks = activeTasks.size + completedTasks.size

    if (totalTasks == 0) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "📝",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bugun uchun vazifa yo‘q",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+ tugmasini bosib qo‘shing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (activeTasks.isNotEmpty()) {
                item(key = "header_active") {
                    Text(
                        text = "Vazifalar (${activeTasks.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(activeTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleCompletion = onToggleCompletion,
                        onEdit = onEditTask,
                        onDelete = onDeleteTask
                    )
                }
            }

            if (completedTasks.isNotEmpty()) {
                item(key = "header_completed") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCompletedExpanded() }
                            .padding(top = 16.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bajarilganlar (${completedTasks.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (completedExpanded) "Yashirish ▲" else "Ko‘rsatish ▼",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (completedExpanded) {
                    items(completedTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggleCompletion = onToggleCompletion,
                            onEdit = onEditTask,
                            onDelete = onDeleteTask
                        )
                    }
                }
            }
        }
    }
}
