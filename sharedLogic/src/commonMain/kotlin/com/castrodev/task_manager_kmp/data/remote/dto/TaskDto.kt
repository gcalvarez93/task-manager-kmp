// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/dto/TaskDto.kt
package com.castrodev.task_manager_kmp.data.remote.dto

import kotlinx.serialization.Serializable

/** Tarea tal y como la devuelve la API. Las fechas llegan como texto ISO-8601. */
@Serializable
internal data class TaskDto(
    val id: String,
    val boardId: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val dueDate: String? = null,
    val labels: List<String> = emptyList(),
    val createdAt: String,
)

/** Cuerpo de `POST /api/tasks`. */
@Serializable
internal data class CreateTaskDto(
    val boardId: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val dueDate: String? = null,
    val labels: List<String> = emptyList(),
)

/** Cuerpo de `PUT /api/tasks/{id}`. La API reemplaza la tarea entera, asi que hay que enviar todos los campos. */
@Serializable
internal data class UpdateTaskDto(
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val dueDate: String? = null,
    val labels: List<String> = emptyList(),
)