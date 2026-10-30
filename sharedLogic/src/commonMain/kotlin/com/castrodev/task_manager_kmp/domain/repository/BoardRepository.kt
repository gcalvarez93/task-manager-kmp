// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/repository/BoardRepository.kt
package com.castrodev.task_manager_kmp.domain.repository

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.Board
import com.castrodev.task_manager_kmp.domain.model.NewBoard

interface BoardRepository {
    suspend fun getBoards(): AppResult<List<Board>>

    suspend fun createBoard(board: NewBoard): AppResult<Board>

    suspend fun deleteBoard(id: String): AppResult<Unit>
}