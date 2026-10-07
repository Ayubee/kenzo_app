package com.example.kunlikvazifalar.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.ui.components.KenzoPanel
import com.example.kunlikvazifalar.ui.components.TaskCard
import com.example.kunlikvazifalar.ui.components.systemAnimationsEnabled

@Composable
fun TodayTasksScreen(
    username: String?,
    activeTasks: List<Task>,
    completedTasks: List<Task>,
    completedExpanded: Boolean,
    onToggleCompletedExpanded: () -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    pendingTaskIds: Set<Long> = emptySet()
) {
    val total = activeTasks.size + completedTasks.size
    val completedCount = completedTasks.size
    val animate = animationsEnabled && systemAnimationsEnabled()

    val greetingName = if (!username.isNullOrBlank()) username else "Foydalanuvchi"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Salomlashuv: "[Username], xush kelibsiz!"
        item(key = "greeting_section") {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
                Text(
                    text = "$greetingName,\nxush kelibsiz!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 36.sp
                )
            }
        }

        // 2. Progress qismi: "3 / 5 bajarildi", nuqtalar va ixcham chiziq
        item(key = "progress_section") {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Option 4 uslubidagi nuqtalar yoki ixcham indikator
                    if (total in 1..7) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until total) {
                                val isDone = i < completedCount
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isDone) {
                                                Modifier.background(MaterialTheme.colorScheme.primary)
                                            } else {
                                                Modifier
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = MaterialTheme.colorScheme.outlineVariant,
                                                        shape = CircleShape
                                                    )
                                                    .background(MaterialTheme.colorScheme.surface)
                                            }
                                        )
                                )
                            }
                        }
                    } else if (total > 7) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = completedCount.toFloat() / total,
                            animationSpec = tween(if (animate) 200 else 0),
                            label = "progress-bar"
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (total > 0) {
                        Text(
                            text = "$completedCount / $total bajarildi",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Matnli aniq progress tavsifi
                if (total > 0) {
                    Text(
                        text = "Bugun $total ta vazifadan $completedCount tasi bajarildi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bo'lim nomi (Option 4: "Bugungi vazifalar")
                Text(
                    text = "Bugungi vazifalar",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. Agar vazifalar bo'lmasa, iliq toza bo'sh holat kartochkasi
        if (total == 0) {
            item(key = "empty_today") {
                KenzoPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddTask,
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "Bugun uchun vazifa yo‘q",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "+ tugmasini bosib vazifa qo‘shing. Vaqt va prioritetni o‘zingizga moslang.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Faol vazifalar ro'yxati (bitta ustunda)
        if (activeTasks.isNotEmpty()) {
            items(activeTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleCompletion = onToggleCompletion,
                    onEdit = onEditTask,
                    onDelete = onDeleteTask,
                    modifier = if (animate) {
                        Modifier.animateItem(
                            fadeInSpec = tween(200),
                            placementSpec = tween(200),
                            fadeOutSpec = tween(200)
                        )
                    } else Modifier,
                    animationsEnabled = animate,
                    busy = task.id in pendingTaskIds
                )
            }
        }

        // 5. Bajarilganlar bo'limi (alohida saqlangan va buklanuvchi)
        if (completedTasks.isNotEmpty()) {
            item(key = "header_completed") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleCompletedExpanded)
                        .heightIn(min = 44.dp)
                        .padding(top = 10.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bajarilganlar (${completedTasks.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (completedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (completedExpanded) "Bajarilganlarni yashirish" else "Bajarilganlarni ko‘rsatish",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (completedExpanded) {
                items(completedTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleCompletion = onToggleCompletion,
                        onEdit = onEditTask,
                        onDelete = onDeleteTask,
                        modifier = if (animate) {
                            Modifier.animateItem(
                                fadeInSpec = tween(200),
                                placementSpec = tween(200),
                                fadeOutSpec = tween(200)
                            )
                        } else Modifier,
                        animationsEnabled = animate,
                        busy = task.id in pendingTaskIds
                    )
                }
            }
        }
    }
}
