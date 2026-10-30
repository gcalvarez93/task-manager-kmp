// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/model/NewBoard.kt
package com.castrodev.task_manager_kmp.domain.model

/** Datos necesarios para crear un tablero. */
data class NewBoard(
    val name: String,
    val description: String = "",
    val color: String,
)