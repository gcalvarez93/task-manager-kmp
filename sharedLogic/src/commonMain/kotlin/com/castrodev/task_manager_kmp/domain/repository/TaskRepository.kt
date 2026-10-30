// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/repository/TaskRepository.kt
package com.castrodev.task_manager_kmp.domain.repository

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem

interface TaskRepository {
    /** Devuelve las tareas del usuario; si se indica [boardId], solo las de ese tablero. */
    suspend fun getTasks(boardId: String? = null): AppResult<List<TaskItem>>

    suspend fun createTask(task: NewTask): AppResult<TaskItem>

    /** Reemplaza la tarea completa en el servidor (la API no admite cambios parciales). */
    suspend fun updateTask(task: TaskItem): AppResult<TaskItem>

    suspend fun deleteTask(id: String): AppResult<Unit>
}