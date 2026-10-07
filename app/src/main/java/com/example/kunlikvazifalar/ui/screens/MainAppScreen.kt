package com.example.kunlikvazifalar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var historyTaskToReAdd by remember { mutableStateOf<Task?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Xabarlar oqimini kuzatish
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val greetingText = if (!uiState.username.isNullOrBlank()) {
        "${uiState.username}, xush kelibsiz!"
    } else {
        "Xush kelibsiz!"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = greetingText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "1.0.0v",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Sozlamalar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Bugun") },
                    label = { Text("Bugun") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Tarix") },
                    label = { Text("Tarix") }
                )
            }
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Yangi vazifa qo‘shish"
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
                        activeTasks = uiState.todayActiveTasks,
                        completedTasks = uiState.todayCompletedTasks,
                        completedExpanded = uiState.completedSectionExpanded,
                        onToggleCompletedExpanded = { viewModel.toggleCompletedSectionExpanded() },
                        onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = { taskToEdit = it },
                        onDeleteTask = { taskToDelete = it }
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
            onConfirm = { text, time ->
                showAddTaskDialog = false
                viewModel.addTask(text, time)
            },
            isSaving = uiState.isSaving
        )
    }

    if (taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            onDismiss = { taskToEdit = null },
            onConfirm = { newText, newTime ->
                val target = taskToEdit!!
                taskToEdit = null
                viewModel.updateTask(target, newText, newTime)
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
            onConfirm = { target, newTime ->
                historyTaskToReAdd = null
                viewModel.reAddHistoryTaskToToday(target, newTime)
            },
            isSaving = uiState.isSaving
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentUsername = uiState.username ?: "",
            onDismiss = { showSettingsDialog = false },
            onSaveUsername = { newName ->
                viewModel.saveUsername(newName)
            }
        )
    }
}
