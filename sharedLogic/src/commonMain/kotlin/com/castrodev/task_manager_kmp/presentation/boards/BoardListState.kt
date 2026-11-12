// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/boards/BoardListState.kt
package com.castrodev.task_manager_kmp.presentation.boards

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.domain.model.Board

/**
 * Estado de la pantalla de tableros. Es inmutable: las vistas solo lo leen.
 *
 * @property error ultimo error pendiente de mostrar como aviso (borrar, o recargar con datos ya en pantalla).
 * @property loadError la carga ha fallado y no hay nada que mostrar: la pantalla debe ofrecer reintentar
 * en lugar de parecer una lista vacia. Se limpia al empezar una nueva carga.
 * @property isCreateFormOpen el formulario de crear tablero esta abierto. Solo se cierra al crear
 * correctamente o cuando el usuario lo cancela: si falla, sigue abierto y conserva lo escrito.
 * @property isCreating hay un intento de crear en curso.
 * @property createError error del ultimo intento de crear; se muestra dentro del formulario.
 */
data class BoardListState(
    val isLoading: Boolean = false,
    val boards: List<Board> = emptyList(),
    val error: AppError? = null,
    val loadError: AppError? = null,
    val isCreateFormOpen: Boolean = false,
    val isCreating: Boolean = false,
    val createError: AppError? = null,
)