// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/repository/LoggingRepositories.kt
package com.castrodev.task_manager_kmp.data.repository

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.core.ErrorLogger
import com.castrodev.task_manager_kmp.domain.model.Board
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository

/**
 * Decoradores que registran en el [ErrorLogger] cada operacion que falla y devuelven el resultado
 * tal cual. Asi el detalle tecnico (codigo HTTP, texto del servidor, tipo de excepcion) queda en el log
 * sin que los repositorios de Ktor ni la UI tengan que ocuparse de ello.
 */
internal class LoggingTaskRepository(
    private val delegate: TaskRepository,
    private val logger: ErrorLogger,
) : TaskRepository {

    override suspend fun getTasks(boardId: String?): AppResult<List<TaskItem>> =
        delegate.getTasks(boardId).logFailure(logger, "tareas.getTasks")

    override suspend fun createTask(task: NewTask): AppResult<TaskItem> =
        delegate.createTask(task).logFailure(logger, "tareas.createTask")

    override suspend fun updateTask(task: TaskItem): AppResult<TaskItem> =
        delegate.updateTask(task).logFailure(logger, "tareas.updateTask")

    override suspend fun deleteTask(id: String): AppResult<Unit> =
        delegate.deleteTask(id).logFailure(logger, "tareas.deleteTask")
}

internal class LoggingBoardRepository(
    private val delegate: BoardRepository,
    private val logger: ErrorLogger,
) : BoardRepository {

    override suspend fun getBoards(): AppResult<List<Board>> =
        delegate.getBoards().logFailure(logger, "tableros.getBoards")

    override suspend fun createBoard(board: NewBoard): AppResult<Board> =
        delegate.createBoard(board).logFailure(logger, "tableros.createBoard")

    override suspend fun deleteBoard(id: String): AppResult<Unit> =
        delegate.deleteBoard(id).logFailure(logger, "tableros.deleteBoard")
}

private fun <T> AppResult<T>.logFailure(logger: ErrorLogger, operation: String): AppResult<T> {
    if (this is AppResult.Failure) {
        logger.warn("$operation ha fallado: $error")
    }
    return this
}