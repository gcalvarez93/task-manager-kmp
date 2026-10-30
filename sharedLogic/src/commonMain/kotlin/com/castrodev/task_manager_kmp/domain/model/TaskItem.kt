// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/model/TaskItem.kt
package com.castrodev.task_manager_kmp.domain.model

import kotlin.time.Instant

/** Tarea que pertenece a un tablero. */
data class TaskItem(
    val id: String,
    val boardId: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueDate: Instant?,
    val labels: List<String>,
    val createdAt: Instant,
)