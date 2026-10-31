// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/testing/FakeBoardRepository.kt
package com.castrodev.task_manager_kmp.testing

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.Board
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Instant

/**
 * Repositorio de tableros en memoria para los tests.
 * - [failWith]: si no es `null`, todas las operaciones devuelven ese error.
 * - [gate]: si no es `null`, [getBoards] espera a que se complete (para observar el estado de carga).
 */
internal class FakeBoardRepository(initial: List<Board> = emptyList()) : BoardRepository {
    val boards: MutableList<Board> = initial.toMutableList()
    val created = mutableListOf<NewBoard>()
    var failWith: AppError? = null
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getBoards(): AppResult<List<Board>> {
        gate?.await()
        failWith?.let { return AppResult.Failure(it) }
        return AppResult.Success(boards.toList())
    }

    override suspend fun createBoard(board: NewBoard): AppResult<Board> {
        failWith?.let { return AppResult.Failure(it) }
        created += board
        val item = boardItem(id = "created-${created.size}", name = board.name, color = board.color)
        boards += item
        return AppResult.Success(item)
    }

    override suspend fun deleteBoard(id: String): AppResult<Unit> {
        failWith?.let { return AppResult.Failure(it) }
        return if (boards.removeAll { it.id == id }) {
            AppResult.Success(Unit)
        } else {
            AppResult.Failure(AppError.NotFound)
        }
    }
}

internal fun boardItem(
    id: String = "1",
    name: String = "Tablero $id",
    color: String = "#FF9800",
): Board = Board(
    id = id,
    name = name,
    description = "",
    color = color,
    taskCount = 0,
    createdAt = Instant.parse("2026-10-30T19:00:00Z"),
)