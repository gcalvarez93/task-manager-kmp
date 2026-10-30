// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/mapper/BoardMapper.kt
package com.castrodev.task_manager_kmp.data.mapper

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.data.remote.dto.BoardDto
import com.castrodev.task_manager_kmp.data.remote.dto.CreateBoardDto
import com.castrodev.task_manager_kmp.domain.model.Board
import com.castrodev.task_manager_kmp.domain.model.NewBoard

/** Convierte el tablero de la API en un modelo de dominio, o devuelve [AppError.InvalidData] si la fecha no es valida. */
internal fun BoardDto.toDomain(): AppResult<Board> {
    val created = parseInstantOrNull(createdAt)
        ?: return AppResult.Failure(AppError.InvalidData("Fecha de creacion invalida: $createdAt"))

    return AppResult.Success(
        Board(
            id = id,
            name = name,
            description = description,
            color = color,
            taskCount = taskCount,
            createdAt = created,
        ),
    )
}

/** Convierte una lista completa; si un tablero no es valido, devuelve ese error. */
internal fun List<BoardDto>.toDomainList(): AppResult<List<Board>> {
    val boards = ArrayList<Board>(size)
    for (dto in this) {
        when (val result = dto.toDomain()) {
            is AppResult.Success -> boards.add(result.value)
            is AppResult.Failure -> return result
        }
    }
    return AppResult.Success(boards)
}

internal fun NewBoard.toCreateDto(): CreateBoardDto = CreateBoardDto(
    name = name,
    description = description,
    color = color,
)