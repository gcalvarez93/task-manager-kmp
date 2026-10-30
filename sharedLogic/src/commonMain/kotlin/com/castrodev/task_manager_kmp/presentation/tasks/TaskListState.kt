// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/tasks/TaskListState.kt
package com.castrodev.task_manager_kmp.presentation.tasks

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskStatus

/**
 * Estado completo de la pantalla de tareas. Es inmutable: las vistas solo lo leen.
 *
 * @property allTasks todas las tareas recibidas de la API.
 * @property visibleTasks las tareas que se muestran: filtradas por [filter] y ordenadas.
 * @property filter estado por el que se filtra, o `null` para mostrar todas.
 * @property error ultimo error pendiente de mostrar al usuario.
 */
data class TaskListState(
    val isLoading: Boolean = false,
    val allTasks: List<TaskItem> = emptyList(),
    val visibleTasks: List<TaskItem> = emptyList(),
    val filter: TaskStatus? = null,
    val error: AppError? = null,
)