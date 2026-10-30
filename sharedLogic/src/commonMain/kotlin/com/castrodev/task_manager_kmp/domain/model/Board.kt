// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/model/Board.kt
package com.castrodev.task_manager_kmp.domain.model

import kotlin.time.Instant

/** Tablero que agrupa tareas. */
data class Board(
    val id: String,
    val name: String,
    val description: String,
    val color: String,
    val taskCount: Int,
    val createdAt: Instant,
)