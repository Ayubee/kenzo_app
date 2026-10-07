package com.example.kunlikvazifalar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.preferences.UserPreferences
import com.example.kunlikvazifalar.data.repository.TaskRepository
import com.example.kunlikvazifalar.notification.ReminderScheduleResult
import com.example.kunlikvazifalar.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val username: String? = null,
    val selectedTab: Int = 0, // 0: Bugun, 1: Tarix
    val todayActiveTasks: List<Task> = emptyList(),
    val todayCompletedTasks: List<Task> = emptyList(),
    val historyGroupedTasks: Map<String, List<Task>> = emptyMap(),
    val isSaving: Boolean = false,
    val completedSectionExpanded: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(application)
    private val preferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(
        MainUiState(
            username = preferences.getUsername()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        loadData()
    }

    fun isFirstLaunch(): Boolean {
        return preferences.getUsername().isNullOrBlank()
    }

    fun isNotificationPrompted(): Boolean {
        return preferences.isNotificationPermissionRequested()
    }

    fun setNotificationPrompted() {
        preferences.setNotificationPermissionRequested(true)
    }

    fun saveUsername(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            preferences.setUsername(trimmed)
            _uiState.update { it.copy(username = trimmed) }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        loadData()
    }

    fun toggleCompletedSectionExpanded() {
        _uiState.update { it.copy(completedSectionExpanded = !it.completedSectionExpanded) }
    }

    fun loadData() {
        viewModelScope.launch {
            val today = DateUtils.getTodayDate()
            val allTodayTasks = repository.getTodayTasks(today)

            val active = allTodayTasks.filter { !it.isCompleted }
            val completed = allTodayTasks.filter { it.isCompleted }

            val historyTasks = repository.getHistoryTasks(today)
            val groupedHistory = historyTasks.groupBy { it.date }

            _uiState.update {
                it.copy(
                    todayActiveTasks = active,
                    todayCompletedTasks = completed,
                    historyGroupedTasks = groupedHistory
                )
            }
        }
    }

    fun addTask(text: String, time: String?) {
        if (_uiState.value.isSaving) return
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val today = DateUtils.getTodayDate()
                val (_, result) = repository.addTask(trimmed, today, time)
                handleReminderResult(result)
                loadData()
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun updateTask(task: Task, newText: String, newTime: String?) {
        if (_uiState.value.isSaving) return
        val trimmed = newText.trim()
        if (trimmed.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val updatedTask = task.copy(
                    text = trimmed,
                    time = if (newTime.isNullOrBlank()) null else newTime
                )
                val (_, result) = repository.updateTask(updatedTask)
                handleReminderResult(result)
                loadData()
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            loadData()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            loadData()
        }
    }

    fun reAddHistoryTaskToToday(originalTask: Task, newTime: String?) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val (_, result) = repository.reAddHistoryTaskToToday(originalTask, newTime)
                handleReminderResult(result)
                loadData()
                _userMessage.emit("Vazifa bugun uchun qo‘shildi!")
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private suspend fun handleReminderResult(result: ReminderScheduleResult) {
        when (result) {
            ReminderScheduleResult.TOO_CLOSE_OR_PAST -> {
                _userMessage.emit("Vaqtgacha 5 daqiqadan kam qolgani sababli eslatma belgilanmadi.")
            }
            ReminderScheduleResult.SCHEDULED -> {
                _userMessage.emit("Vazifa va 5 daqiqa oldingi eslatma saqlandi.")
            }
            ReminderScheduleResult.NO_TIME -> {
                // Vaqtsiz saqlandi, xabar shart emas
            }
            ReminderScheduleResult.PERMISSION_DENIED -> {
                _userMessage.emit("Bildirishnoma ruxsati berilmaganligi sababli eslatma belgilanmadi.")
            }
        }
    }
}
