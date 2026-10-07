package com.example.kunlikvazifalar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.ui.components.KenzoPanel
import com.example.kunlikvazifalar.util.DateUtils

@Composable
fun HistoryScreen(
    groupedTasks: Map<String, List<Task>>,
    onReAddClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 22.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "history_title") {
            Text("Har bir kun —\nyangi tajriba.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text("Avvalgi rejalaringiz shu yerda saqlanadi.", modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (groupedTasks.isEmpty()) {
            item(key = "history_empty") {
                KenzoPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.padding(14.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Text("Tarix hozircha bo‘sh", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Avvalgi kunlardagi vazifalar sana bo‘yicha ko‘rinadi. Ularni bugungi rejaga yana qo‘shishingiz mumkin.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        groupedTasks.forEach { (date, tasks) ->
            item(key = "header_$date") {
                Text(DateUtils.formatUzbekDateHeader(date), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            items(tasks, key = { it.id }) { task ->
                HistoryTaskCard(task, onReAddClick = { onReAddClick(task) }, onDeleteClick = { onDeleteClick(task) })
            }
        }
    }
}

@Composable
private fun HistoryTaskCard(task: Task, onReAddClick: () -> Unit, onDeleteClick: () -> Unit) {
    KenzoPanel(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(task.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "${task.text}: o‘chirish", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Keep status and action on separate lines so both survive large text and narrow screens.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (task.isCompleted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (task.isCompleted) "Bajarilgan" else "Bajarilmagan",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
                if (!task.time.isNullOrBlank()) {
                    Spacer(Modifier.width(10.dp))
                    Text(task.time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val (pBg, pText) = when (task.priority) {
                com.example.kunlikvazifalar.data.model.TaskPriority.LOW ->
                    if (isDark) Pair(com.example.kunlikvazifalar.theme.PriorityLowBgDark, com.example.kunlikvazifalar.theme.PriorityLowTextDark)
                    else Pair(com.example.kunlikvazifalar.theme.PriorityLowBgLight, com.example.kunlikvazifalar.theme.PriorityLowTextLight)
                com.example.kunlikvazifalar.data.model.TaskPriority.NORMAL ->
                    if (isDark) Pair(com.example.kunlikvazifalar.theme.PriorityNormalBgDark, com.example.kunlikvazifalar.theme.PriorityNormalTextDark)
                    else Pair(com.example.kunlikvazifalar.theme.PriorityNormalBgLight, com.example.kunlikvazifalar.theme.PriorityNormalTextLight)
                com.example.kunlikvazifalar.data.model.TaskPriority.HIGH ->
                    if (isDark) Pair(com.example.kunlikvazifalar.theme.PriorityHighBgDark, com.example.kunlikvazifalar.theme.PriorityHighTextDark)
                    else Pair(com.example.kunlikvazifalar.theme.PriorityHighBgLight, com.example.kunlikvazifalar.theme.PriorityHighTextLight)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = pBg
            ) {
                Text(
                    text = task.priority.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = pText,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onReAddClick, shape = RoundedCornerShape(12.dp), modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Bugunga qayta qo‘shish")
            }
        }
    }
}
