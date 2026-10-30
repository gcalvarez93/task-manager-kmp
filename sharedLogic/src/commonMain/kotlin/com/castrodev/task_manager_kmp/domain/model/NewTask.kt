// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/model/NewTask.kt
package com.castrodev.task_manager_kmp.domain.model

import kotlin.time.Instant

/** Datos necesarios para crear una tarea. El servidor asigna el identificador y la fecha de creacion. */
data class NewTask(
    val boardId: String,
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: Instant? = null,
    val labels: List<String> = emptyList(),
)