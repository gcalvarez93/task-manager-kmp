// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/mapper/TaskMapper.kt
package com.castrodev.task_manager_kmp.data.mapper

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.data.remote.dto.CreateTaskDto
import com.castrodev.task_manager_kmp.data.remote.dto.TaskDto
import com.castrodev.task_manager_kmp.data.remote.dto.UpdateTaskDto
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskPriority
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import kotlin.time.Instant

private fun invalid(message: String) = AppResult.Failure(AppError.InvalidData(message))

/** Convierte la tarea de la API en un modelo de dominio, o devuelve [AppError.InvalidData] si algun dato no es valido. */
internal fun TaskDto.toDomain(): AppResult<TaskItem> {
    val taskStatus = TaskStatus.fromApiValue(status)
        ?: return invalid("Estado de tarea desconocido: $status")
    val taskPriority = TaskPriority.fromApiValue(priority)
        ?: return invalid("Prioridad de tarea desconocida: $priority")
    val created = parseInstantOrNull(createdAt)
        ?: return invalid("Fecha de creacion invalida: $createdAt")
    val due: Instant? = when (dueDate) {
        null -> null
        else -> parseInstantOrNull(dueDate) ?: return invalid("Fecha limite invalida: $dueDate")
    }

    return AppResult.Success(
        TaskItem(
            id = id,
            boardId = boardId,
            title = title,
            description = description,
            status = taskStatus,
            priority = taskPriority,
            dueDate = due,
            labels = labels,
            createdAt = created,
        ),
    )
}

/** Convierte una lista completa; si una tarea no es valida, devuelve ese error. */
internal fun List<TaskDto>.toDomainList(): AppResult<List<TaskItem>> {
    val tasks = ArrayList<TaskItem>(size)
    for (dto in this) {
        when (val result = dto.toDomain()) {
            is AppResult.Success -> tasks.add(result.value)
            is AppResult.Failure -> return result
        }
    }
    return AppResult.Success(tasks)
}

internal fun NewTask.toCreateDto(): CreateTaskDto = CreateTaskDto(
    boardId = boardId,
    title = title,
    description = description,
    priority = priority.apiValue,
    status = status.apiValue,
    dueDate = dueDate?.toString(),
    labels = labels,
)

internal fun TaskItem.toUpdateDto(): UpdateTaskDto = UpdateTaskDto(
    title = title,
    description = description,
    status = status.apiValue,
    priority = priority.apiValue,
    dueDate = dueDate?.toString(),
    labels = labels,
)