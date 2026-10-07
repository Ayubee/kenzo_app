package com.example.kunlikvazifalar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.model.TaskPriority
import com.example.kunlikvazifalar.data.preferences.UserPreferences
import com.example.kunlikvazifalar.data.preferences.ThemeMode
import com.example.kunlikvazifalar.notification.NotificationHelper
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import com.example.kunlikvazifalar.theme.PlatformTheme

data class MainUiState(
    val username: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedTab: Int = 0, // 0: Bugun, 1: Tarix
    val todayActiveTasks: List<Task> = emptyList(),
    val todayCompletedTasks: List<Task> = emptyList(),
    val historyGroupedTasks: Map<String, List<Task>> = emptyMap(),
    val isSaving: Boolean = false,
    val completedSectionExpanded: Boolean = true,
    val repeatReminders: Boolean = true,
    val animations: Boolean = true,
    val haptics: Boolean = true,
    val pendingTaskIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val openTask: Task? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(application)
    private val preferences = UserPreferences(application)
    private val dataMutex = Mutex()

    private val _uiState = MutableStateFlow(
        MainUiState(
            username = preferences.getUsername(),
            themeMode = preferences.getThemeMode(),
            repeatReminders = preferences.repeatsEnabled(),
            animations = preferences.animationsEnabled(),
            haptics = preferences.hapticsEnabled()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        loadData()
        viewModelScope.launch {
            var date = DateUtils.getTodayDate()
            while (true) {
                delay(30_000)
                val current = DateUtils.getTodayDate()
                if (date != current) { date = current; refreshAfterResume() }
            }
        }
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

    fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
        PlatformTheme.apply(getApplication(), mode)
    }

    fun setRepeatReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    synchronized(NotificationHelper.reminderLock) {
                        preferences.setRepeatsEnabled(enabled)
                        NotificationHelper.rescheduleReminders(getApplication())
                    }
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _userMessage.emit("Tanlov saqlandi, ammo eslatmalarni qayta belgilab bo‘lmadi.")
            } finally {
                _uiState.update { it.copy(repeatReminders = preferences.repeatsEnabled()) }
            }
        }
    }

    fun setAnimations(enabled: Boolean) {
        preferences.setAnimationsEnabled(enabled)
        _uiState.update { it.copy(animations = enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        preferences.setHapticsEnabled(enabled)
        _uiState.update { it.copy(haptics = enabled) }
    }

    fun openTask(id: Long) {
        viewModelScope.launch {
            try {
                val task = withContext(Dispatchers.IO) { repository.getTask(id) }
                _uiState.update { it.copy(openTask = task, selectedTab = if (task != null && task.date < DateUtils.getTodayDate()) 1 else 0) }
                if (task == null) _userMessage.emit("Bu vazifa endi mavjud emas.")
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _userMessage.emit("Vazifani ochib bo‘lmadi.")
            }
        }
    }

    fun closeOpenTask() { _uiState.update { it.copy(openTask = null) } }

    fun refreshAfterResume() {
        loadData()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                NotificationHelper.rescheduleReminders(getApplication())
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _userMessage.emit("Eslatmalarni tiklab bo‘lmadi. Ilovani qayta ochib ko‘ring.")
            }
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
        if (_uiState.value.loadError != null) _uiState.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            try {
                dataMutex.withLock { reloadData() }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _uiState.update { it.copy(isLoading = false, loadError = "Vazifalarni yuklab bo‘lmadi. Qayta urinib ko‘ring.") }
            }
        }
    }

    private suspend fun reloadData() {
        val today = DateUtils.getTodayDate()
        val (todayTasks, historyTasks) = withContext(Dispatchers.IO) {
            repository.getTodayTasks(today) to repository.getHistoryTasks(today)
        }
        _uiState.update {
            it.copy(
                todayActiveTasks = todayTasks.filter { task -> !task.isCompleted },
                todayCompletedTasks = todayTasks.filter { task -> task.isCompleted },
                historyGroupedTasks = historyTasks.groupBy { task -> task.date },
                isLoading = false,
                loadError = null
            )
        }
    }

    fun addTask(text: String, time: String?, priority: TaskPriority = TaskPriority.NORMAL, onSaved: () -> Unit = {}) {
        if (_uiState.value.isSaving) return
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val today = DateUtils.getTodayDate()
                val (_, result) = dataMutex.withLock {
                    val saved = withContext(Dispatchers.IO) { repository.addTask(trimmed, today, time, priority) }
                    reloadData()
                    saved
                }
                onSaved()
                handleReminderResult(result)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _userMessage.emit("Vazifani saqlab bo‘lmadi. Matn oynada qoldi; qayta urinib ko‘ring.")
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun updateTask(task: Task, newText: String, newTime: String?, priority: TaskPriority = task.priority, onSaved: () -> Unit = {}) {
        if (_uiState.value.isSaving) return
        val trimmed = newText.trim()
        if (trimmed.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val updatedTask = task.copy(
                    text = trimmed,
                    time = if (newTime.isNullOrBlank()) null else newTime,
                    priority = priority
                )
                val (saved, result) = dataMutex.withLock {
                    val outcome = withContext(Dispatchers.IO) { repository.updateTask(updatedTask) }
                    reloadData()
                    outcome
                }
                check(saved) { "Task no longer exists" }
                onSaved()
                handleReminderResult(result)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _userMessage.emit("O‘zgarishni saqlab bo‘lmadi. Qayta urinib ko‘ring.")
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        if (task.id in _uiState.value.pendingTaskIds) return
        _uiState.update { it.copy(pendingTaskIds = it.pendingTaskIds + task.id) }
        viewModelScope.launch {
            try {
                dataMutex.withLock {
                    val saved = withContext(Dispatchers.IO) { repository.toggleTaskCompletion(task) }
                    check(saved) { "Task no longer exists" }
                    reloadData()
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _userMessage.emit("Vazifa holatini o‘zgartirib bo‘lmadi. Qayta urinib ko‘ring.")
            } finally {
                _uiState.update { it.copy(pendingTaskIds = it.pendingTaskIds - task.id) }
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            try {
                dataMutex.withLock {
                    withContext(Dispatchers.IO) { repository.deleteTask(taskId) }
                    reloadData()
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _userMessage.emit("Vazifani o‘chirib bo‘lmadi. Qayta urinib ko‘ring.")
            }
        }
    }

    fun reAddHistoryTaskToToday(originalTask: Task, newTime: String?, priority: TaskPriority = originalTask.priority, onSaved: () -> Unit = {}) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val (_, result) = dataMutex.withLock {
                    val outcome = withContext(Dispatchers.IO) { repository.reAddHistoryTaskToToday(originalTask, newTime, priority) }
                    reloadData()
                    outcome
                }
                onSaved()
                handleReminderResult(result)
                _userMessage.emit("Vazifa bugun uchun qo‘shildi!")
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _userMessage.emit("Vazifani bugunga qo‘shib bo‘lmadi. Qayta urinib ko‘ring.")
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private suspend fun handleReminderResult(result: ReminderScheduleResult) {
        when (result) {
            ReminderScheduleResult.TOO_CLOSE_OR_PAST -> {
                _userMessage.emit("Vazifa saqlandi. Kelajakda yuborilishi kerak bo‘lgan eslatma qolmadi.")
            }
            ReminderScheduleResult.SCHEDULED -> {
                _userMessage.emit("Vazifa va kelajakdagi eslatmalar saqlandi.")
            }
            ReminderScheduleResult.NO_TIME -> {
                // Vaqtsiz saqlandi, xabar shart emas
            }
            ReminderScheduleResult.PERMISSION_DENIED -> {
                _userMessage.emit("Vazifa saqlandi. Bildirishnomalar o‘chiq: eslatma uchun Sozlamalarda ruxsat bering.")
            }
            ReminderScheduleResult.SCHEDULED_APPROXIMATE -> {
                _userMessage.emit("Vazifa saqlandi. Aniq signal ruxsati yo‘q: eslatma kechikishi mumkin. Sozlamalarda yoqing.")
            }
            ReminderScheduleResult.SCHEDULING_FAILED -> {
                _userMessage.emit("Vazifa saqlandi, ammo eslatmani belgilab bo‘lmadi.")
            }
        }
    }
}
