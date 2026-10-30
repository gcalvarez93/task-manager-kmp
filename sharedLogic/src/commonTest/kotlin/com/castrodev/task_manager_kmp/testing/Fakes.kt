// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/testing/Fakes.kt
package com.castrodev.task_manager_kmp.testing

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskPriority
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Instant

/**
 * Repositorio en memoria para los tests.
 * - [failWith]: si no es `null`, todas las operaciones devuelven ese error.
 * - [gate]: si no es `null`, [getTasks] espera a que se complete (para observar el estado de carga).
 */
internal class FakeTaskRepository(initial: List<TaskItem> = emptyList()) : TaskRepository {
    val tasks: MutableList<TaskItem> = initial.toMutableList()
    val created = mutableListOf<NewTask>()
    var failWith: AppError? = null
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getTasks(boardId: String?): AppResult<List<TaskItem>> {
        gate?.await()
        failWith?.let { return AppResult.Failure(it) }
        return AppResult.Success(tasks.toList())
    }

    override suspend fun createTask(task: NewTask): AppResult<TaskItem> {
        failWith?.let { return AppResult.Failure(it) }
        created += task
        val item = taskItem(id = "created-${created.size}", title = task.title, boardId = task.boardId)
        tasks += item
        return AppResult.Success(item)
    }

    override suspend fun updateTask(task: TaskItem): AppResult<TaskItem> {
        failWith?.let { return AppResult.Failure(it) }
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index < 0) return AppResult.Failure(AppError.NotFound)
        tasks[index] = task
        return AppResult.Success(task)
    }

    override suspend fun deleteTask(id: String): AppResult<Unit> {
        failWith?.let { return AppResult.Failure(it) }
        return if (tasks.removeAll { it.id == id }) {
            AppResult.Success(Unit)
        } else {
            AppResult.Failure(AppError.NotFound)
        }
    }
}

internal fun taskItem(
    id: String = "1",
    boardId: String = "board-1",
    title: String = "Tarea $id",
    status: TaskStatus = TaskStatus.PENDING,
    priority: TaskPriority = TaskPriority.MEDIUM,
    dueDate: Instant? = null,
): TaskItem = TaskItem(
    id = id,
    boardId = boardId,
    title = title,
    description = "",
    status = status,
    priority = priority,
    dueDate = dueDate,
    labels = emptyList(),
    createdAt = Instant.parse("2026-10-30T19:00:00Z"),
)