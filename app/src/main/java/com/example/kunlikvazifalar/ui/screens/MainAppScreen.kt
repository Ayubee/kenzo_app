package com.example.kunlikvazifalar.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kunlikvazifalar.R
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.ui.MainViewModel
import com.example.kunlikvazifalar.ui.components.AddTaskDialog
import com.example.kunlikvazifalar.ui.components.DeleteConfirmDialog
import com.example.kunlikvazifalar.ui.components.EditTaskDialog
import com.example.kunlikvazifalar.ui.components.ReAddTaskDialog
import com.example.kunlikvazifalar.ui.components.SettingsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val toggle: (Task) -> Unit = { task ->
        if (task.id !in uiState.pendingTaskIds) {
            viewModel.toggleTaskCompletion(task)
            if (uiState.haptics && !task.isCompleted) {
                view.performHapticFeedback(
                    if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
                )
            }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var historyTaskToReAdd by remember { mutableStateOf<Task?>(null) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    // Xabarlar oqimini kuzatish
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Option 4: Ixcham Kenzo App sarlavhasi (sariq K logosi bilan) va uch nuqtali sozlamalar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_kenzo),
                            contentDescription = "Kenzo App logosi",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(7.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Kenzo App",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Sozlamalar va ma’lumotlar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Option 4: Iliq minimal pastki navigatsiya
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Bugun", modifier = Modifier.size(20.dp)) },
                        label = { Text("Bugun") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(Icons.Default.Schedule, contentDescription = "Tarix", modifier = Modifier.size(20.dp)) },
                        label = { Text("Tarix") },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                // Option 4: Terrakota rangli, doira shaklidagi oq "+" tugmasi
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Yangi vazifa qo‘shish",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> {
                    TodayTasksScreen(
                        username = uiState.username,
                        activeTasks = uiState.todayActiveTasks,
                        completedTasks = uiState.todayCompletedTasks,
                        completedExpanded = uiState.completedSectionExpanded,
                        onToggleCompletedExpanded = { viewModel.toggleCompletedSectionExpanded() },
                        onToggleCompletion = toggle,
                        onEditTask = { taskToEdit = it },
                        onDeleteTask = { taskToDelete = it },
                        animationsEnabled = uiState.animations,
                        pendingTaskIds = uiState.pendingTaskIds
                    )
                }
                1 -> {
                    HistoryScreen(
                        groupedTasks = uiState.historyGroupedTasks,
                        onReAddClick = { historyTaskToReAdd = it },
                        onDeleteClick = { taskToDelete = it }
                    )
                }
            }
        }
    }

    // Dialoglar
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { text, time, priority ->
                viewModel.addTask(text, time, priority) { showAddTaskDialog = false }
            },
            isSaving = uiState.isSaving
        )
    }

    if (taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            onDismiss = { taskToEdit = null },
            onConfirm = { newText, newTime, priority ->
                val target = taskToEdit!!
                viewModel.updateTask(target, newText, newTime, priority) { taskToEdit = null }
            },
            isSaving = uiState.isSaving
        )
    }

    if (taskToDelete != null) {
        DeleteConfirmDialog(
            task = taskToDelete!!,
            onDismiss = { taskToDelete = null },
            onConfirm = { target ->
                taskToDelete = null
                viewModel.deleteTask(target.id)
            }
        )
    }

    if (historyTaskToReAdd != null) {
        ReAddTaskDialog(
            task = historyTaskToReAdd!!,
            onDismiss = { historyTaskToReAdd = null },
            onConfirm = { target, newTime, priority ->
                viewModel.reAddHistoryTaskToToday(target, newTime, priority) { historyTaskToReAdd = null }
            },
            isSaving = uiState.isSaving
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentUsername = uiState.username ?: "",
            themeMode = uiState.themeMode,
            onThemeModeChange = viewModel::setThemeMode,
            onDismiss = { showSettingsDialog = false },
            onSaveUsername = { newName ->
                viewModel.saveUsername(newName)
            },
            repeatReminders = uiState.repeatReminders,
            onRepeatsChange = viewModel::setRepeatReminders,
            animations = uiState.animations,
            onAnimationsChange = viewModel::setAnimations,
            haptics = uiState.haptics,
            onHapticsChange = viewModel::setHaptics
        )
    }

    uiState.openTask?.let { task ->
        AlertDialog(
            onDismissRequest = viewModel::closeOpenTask,
            title = { Text("Vazifa") },
            text = {
                Column {
                    Text(task.text, style = MaterialTheme.typography.bodyLarge)
                    Text("${task.priority.label} · ${task.time ?: "Vaqtsiz"}")
                    Text(if (task.isCompleted) "Bajarilgan" else "Bajarilmagan")
                }
            },
            confirmButton = {
                TextButton(onClick = { toggle(task); viewModel.closeOpenTask() }) {
                    Text(if (task.isCompleted) "Qaytarish" else "Bajarildi")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeOpenTask) {
                    Text("Yopish")
                }
            }
        )
    }
}
