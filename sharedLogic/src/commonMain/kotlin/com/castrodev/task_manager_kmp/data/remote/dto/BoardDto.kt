// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/dto/BoardDto.kt
package com.castrodev.task_manager_kmp.data.remote.dto

import kotlinx.serialization.Serializable

/** Tablero tal y como lo devuelve la API. */
@Serializable
internal data class BoardDto(
    val id: String,
    val name: String,
    val description: String,
    val color: String,
    val taskCount: Int,
    val createdAt: String,
)

/** Cuerpo de `POST /api/tasks/boards`. */
@Serializable
internal data class CreateBoardDto(
    val name: String,
    val description: String,
    val color: String,
)