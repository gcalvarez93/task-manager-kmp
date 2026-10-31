// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/boards/BoardListState.kt
package com.castrodev.task_manager_kmp.presentation.boards

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.domain.model.Board

/**
 * Estado de la pantalla de tableros. Es inmutable: las vistas solo lo leen.
 *
 * @property error ultimo error pendiente de mostrar al usuario.
 */
data class BoardListState(
    val isLoading: Boolean = false,
    val boards: List<Board> = emptyList(),
    val error: AppError? = null,
)