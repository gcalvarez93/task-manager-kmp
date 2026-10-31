// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/usecase/CreateBoardUseCase.kt
package com.castrodev.task_manager_kmp.domain.usecase

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.Board
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository

/**
 * Crea un tablero aplicando las reglas de negocio antes de llamar a la API:
 * el nombre no puede estar vacio y tiene un maximo de [MAX_NAME_LENGTH] caracteres.
 * Los espacios al principio y al final del nombre se eliminan.
 */
class CreateBoardUseCase(private val repository: BoardRepository) {

    suspend operator fun invoke(board: NewBoard): AppResult<Board> {
        val name = board.name.trim()
        if (name.isEmpty()) {
            return AppResult.Failure(AppError.Validation("El nombre no puede estar vacio"))
        }
        if (name.length > MAX_NAME_LENGTH) {
            return AppResult.Failure(
                AppError.Validation("El nombre no puede superar los $MAX_NAME_LENGTH caracteres"),
            )
        }
        return repository.createBoard(board.copy(name = name))
    }

    companion object {
        const val MAX_NAME_LENGTH = 60
    }
}