// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/tasks/TaskListViewModel.kt
package com.castrodev.task_manager_kmp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import com.castrodev.task_manager_kmp.presentation.tasks.TaskListState
import kotlinx.coroutines.flow.StateFlow

/** ViewModel fino: la logica vive en el `TaskListStore` compartido, filtrado por tablero. */
class TaskListViewModel(container: AppContainer, val boardId: String) : ViewModel() {

    private val store = container.createTaskListStore(boardId, viewModelScope)

    val state: StateFlow<TaskListState> = store.state

    init {
        store.refresh()
    }

    fun setFilter(status: TaskStatus?) = store.setFilter(status)

    fun changeStatus(task: TaskItem, status: TaskStatus) = store.changeStatus(task, status)

    fun delete(id: String) = store.delete(id)

    fun create(task: NewTask) = store.create(task)

    fun clearError() = store.clearError()
}