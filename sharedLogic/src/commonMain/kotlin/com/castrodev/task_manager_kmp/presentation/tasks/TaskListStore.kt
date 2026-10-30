// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/tasks/TaskListStore.kt
package com.castrodev.task_manager_kmp.presentation.tasks

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository
import com.castrodev.task_manager_kmp.domain.usecase.CreateTaskUseCase
import com.castrodev.task_manager_kmp.domain.usecase.FilterTasksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Logica de presentacion compartida de la lista de tareas.
 *
 * No depende de `androidx.lifecycle`: el [scope] lo pone cada plataforma
 * (`viewModelScope` en Android, un scope propio en iOS). Asi el modulo compartido
 * no arrastra dependencias de UI y se puede probar con un scope de test.
 *
 * Todas las acciones son asincronas y publican su resultado en [state].
 *
 * @param boardId tablero cuyas tareas se muestran, o `null` para todas.
 */
class TaskListStore(
    private val boardId: String?,
    private val repository: TaskRepository,
    private val createTaskUseCase: CreateTaskUseCase,
    private val filterTasks: FilterTasksUseCase,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    /** Carga las tareas. Si ya hay una carga en curso, no hace nada. */
    fun refresh() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            when (val result = repository.getTasks(boardId)) {
                is AppResult.Success -> _state.update { it.withTasks(result.value).copy(isLoading = false) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    /** Filtra por estado; `null` muestra todas. */
    fun setFilter(status: TaskStatus?) {
        _state.update { it.withTasks(it.allTasks, status) }
    }

    fun changeStatus(task: TaskItem, status: TaskStatus) {
        scope.launch {
            when (val result = repository.updateTask(task.copy(status = status))) {
                is AppResult.Success -> _state.update { current ->
                    current.withTasks(current.allTasks.map { if (it.id == result.value.id) result.value else it })
                }
                is AppResult.Failure -> _state.update { it.copy(error = result.error) }
            }
        }
    }

    fun delete(id: String) {
        scope.launch {
            when (val result = repository.deleteTask(id)) {
                is AppResult.Success -> _state.update { current ->
                    current.withTasks(current.allTasks.filterNot { it.id == id })
                }
                is AppResult.Failure -> _state.update { it.copy(error = result.error) }
            }
        }
    }

    fun create(task: NewTask) {
        scope.launch {
            when (val result = createTaskUseCase(task)) {
                is AppResult.Success -> _state.update { current ->
                    current.withTasks(current.allTasks + result.value)
                }
                is AppResult.Failure -> _state.update { it.copy(error = result.error) }
            }
        }
    }

    /** La vista llama a esto despues de mostrar el error. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun TaskListState.withTasks(
        tasks: List<TaskItem>,
        newFilter: TaskStatus? = filter,
    ): TaskListState = copy(
        allTasks = tasks,
        filter = newFilter,
        visibleTasks = filterTasks(tasks, newFilter),
    )
}