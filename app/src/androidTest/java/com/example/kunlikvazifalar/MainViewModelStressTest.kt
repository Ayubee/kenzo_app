package com.example.kunlikvazifalar

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kunlikvazifalar.data.db.TaskDatabaseHelper
import com.example.kunlikvazifalar.data.model.Task
import com.example.kunlikvazifalar.data.model.TaskPriority
import com.example.kunlikvazifalar.data.repository.TaskRepository
import com.example.kunlikvazifalar.ui.MainViewModel
import com.example.kunlikvazifalar.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class MainViewModelStressTest {
    @Test fun staleEditCannotUndoNotificationCompletion() {
        val app: Application = ApplicationProvider.getApplicationContext()
        TaskDatabaseHelper(app).use { db ->
            val draft = Task(text = "kenzo_edit_${UUID.randomUUID()}", date = DateUtils.getTodayDate())
            val stale = draft.copy(id = db.insertTask(draft))
            try {
                db.setTaskCompleted(stale.id, true)
                assertTrue(TaskRepository(app).updateTask(stale.copy(text = "Edited after completion", priority = TaskPriority.HIGH)).first)
                val actual = db.getTaskById(stale.id)!!
                assertTrue(actual.isCompleted)
                assertEquals("Edited after completion", actual.text)
                assertEquals(TaskPriority.HIGH, actual.priority)
            } finally { db.deleteTask(stale.id) }
        }
    }

    @Test fun rapidRepeatedTapsCommitOneChangeAndKeepProgressConsistent() {
        val app: Application = ApplicationProvider.getApplicationContext()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val store = ViewModelStore()
        TaskDatabaseHelper(app).use { db ->
            val draft = Task(text = "kenzo_stress_${UUID.randomUUID()}", date = DateUtils.getTodayDate())
            val task = draft.copy(id = db.insertTask(draft))
            lateinit var vm: MainViewModel
            try {
                instrumentation.runOnMainSync { vm = MainViewModel(app); store.put("test", vm) }
                await { !vm.uiState.value.isLoading }
                val before = vm.uiState.value.todayCompletedTasks.size
                instrumentation.runOnMainSync { repeat(100) { vm.toggleTaskCompletion(task) } }
                await { task.id !in vm.uiState.value.pendingTaskIds }
                assertTrue(db.getTaskById(task.id)!!.isCompleted)
                assertEquals(before + 1, vm.uiState.value.todayCompletedTasks.size)
                assertEquals(1, (vm.uiState.value.todayActiveTasks + vm.uiState.value.todayCompletedTasks).count { it.id == task.id })
                val completed = db.getTaskById(task.id)!!
                instrumentation.runOnMainSync { repeat(100) { vm.toggleTaskCompletion(completed) } }
                await { task.id !in vm.uiState.value.pendingTaskIds }
                assertFalse(db.getTaskById(task.id)!!.isCompleted)
                assertEquals(before, vm.uiState.value.todayCompletedTasks.size)
            } finally {
                instrumentation.runOnMainSync { store.clear() }
                // Delete only the row created by this test, never reset user storage.
                db.deleteTask(task.id)
            }
        }
    }

    private fun await(condition: () -> Boolean) {
        val end = System.nanoTime() + 5_000_000_000
        while (!condition() && System.nanoTime() < end) Thread.sleep(20)
        assertTrue("Timed out waiting for the actual persisted state", condition())
    }
}
