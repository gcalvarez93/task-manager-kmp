// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/model/Board.kt
@file:OptIn(ExperimentalObjCName::class)

package com.castrodev.task_manager_kmp.domain.model

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlin.time.Instant

/** Tablero que agrupa tareas. */
data class Board(
    val id: String,
    val name: String,
    // En Swift `description` choca con NSObject.description; se expone como `descriptionText`.
    @property:ObjCName("descriptionText")
    val description: String,
    val color: String,
    val taskCount: Int,
    val createdAt: Instant,
)