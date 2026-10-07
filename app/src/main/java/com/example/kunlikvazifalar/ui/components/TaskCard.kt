package com.example.kunlikvazifalar.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.model.TaskPriority
import com.example.kunlikvazifalar.theme.PriorityHighBgDark
import com.example.kunlikvazifalar.theme.PriorityHighBgLight
import com.example.kunlikvazifalar.theme.PriorityHighTextDark
import com.example.kunlikvazifalar.theme.PriorityHighTextLight
import com.example.kunlikvazifalar.theme.PriorityLowBgDark
import com.example.kunlikvazifalar.theme.PriorityLowBgLight
import com.example.kunlikvazifalar.theme.PriorityLowTextDark
import com.example.kunlikvazifalar.theme.PriorityLowTextLight
import com.example.kunlikvazifalar.theme.PriorityNormalBgDark
import com.example.kunlikvazifalar.theme.PriorityNormalBgLight
import com.example.kunlikvazifalar.theme.PriorityNormalTextDark
import com.example.kunlikvazifalar.theme.PriorityNormalTextLight

@Composable
fun TaskCard(
    task: Task,
    onToggleCompletion: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    busy: Boolean = false
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

    val targetCardColor = if (!task.isCompleted && task.priority == TaskPriority.HIGH) {
        lerp(scheme.surface, if (isDark) PriorityHighBgDark else PriorityHighBgLight, 0.45f)
    } else scheme.surface
    val cardBorder = when {
        task.isCompleted || task.priority == TaskPriority.LOW -> scheme.outlineVariant
        task.priority == TaskPriority.HIGH -> lerp(scheme.outlineVariant,
            if (isDark) PriorityHighTextDark else PriorityHighTextLight, 0.5f)
        else -> lerp(scheme.outlineVariant,
            if (isDark) PriorityNormalTextDark else PriorityNormalTextLight, 0.25f)
    }
    val cardColor by animateColorAsState(
        targetValue = targetCardColor,
        animationSpec = tween(if (animationsEnabled) 200 else 0),
        label = "task-card-color"
    )

    // Prioritet ranglari (4. Iliq minimal talabiga binoan terrakotadan ajralib turadi)
    val (priorityBg, priorityTextColor) = if (task.isCompleted) {
        Pair(scheme.surfaceVariant, scheme.onSurfaceVariant)
    } else when (task.priority) {
        TaskPriority.LOW -> if (isDark) Pair(PriorityLowBgDark, PriorityLowTextDark) else Pair(PriorityLowBgLight, PriorityLowTextLight)
        TaskPriority.NORMAL -> if (isDark) Pair(PriorityNormalBgDark, PriorityNormalTextDark) else Pair(PriorityNormalBgLight, PriorityNormalTextLight)
        TaskPriority.HIGH -> if (isDark) Pair(PriorityHighBgDark, PriorityHighTextDark) else Pair(PriorityHighBgLight, PriorityHighTextLight)
    }

    KenzoPanel(
        modifier = modifier.fillMaxWidth(),
        color = cardColor,
        borderColor = cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sokin yashil galochka
            ProgressCheckbox(
                checked = task.isCompleted,
                enabled = !busy,
                animate = animationsEnabled,
                description = if (task.isCompleted) "${task.text}: bajarilmagan deb belgilash" else "${task.text}: bajarildi deb belgilash",
                onClick = { onToggleCompletion(task) }
            )

            Spacer(Modifier.width(8.dp))

            // Matn va qo'shimcha ma'lumotlar ustuni
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !busy) { onToggleCompletion(task) }
                    .heightIn(min = 48.dp)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Vazifa matni
                Text(
                    text = task.text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) {
                        // O'qib bo'lmaydigan darajada xiralashtirilmagan toza kulrang
                        scheme.onSurfaceVariant
                    } else {
                        scheme.onSurface
                    }
                )

                Spacer(Modifier.height(6.dp))

                // Vaqt va prioritet qatori (Option 4 uslubida)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!task.time.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = scheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = task.time,
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Prioritet nishoni
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityBg
                    ) {
                        Text(
                            text = task.priority.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = priorityTextColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Uch nuqtali menyu
            Box {
                IconButton(onClick = { menuExpanded = true }, enabled = !busy) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "${task.text}: amallar menyusi",
                        tint = scheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = scheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text("Tahrirlash") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit(task)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("O‘chirish", color = scheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = scheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete(task)
                        }
                    )
                }
            }
        }
    }
}
