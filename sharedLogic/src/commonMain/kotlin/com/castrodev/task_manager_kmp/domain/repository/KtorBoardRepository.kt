// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/repository/KtorBoardRepository.kt
package com.castrodev.task_manager_kmp.data.repository

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.core.flatMap
import com.castrodev.task_manager_kmp.data.mapper.toCreateDto
import com.castrodev.task_manager_kmp.data.mapper.toDomain
import com.castrodev.task_manager_kmp.data.mapper.toDomainList
import com.castrodev.task_manager_kmp.data.remote.dto.BoardDto
import com.castrodev.task_manager_kmp.data.remote.safeApiCall
import com.castrodev.task_manager_kmp.data.remote.toResult
import com.castrodev.task_manager_kmp.data.remote.toUnitResult
import com.castrodev.task_manager_kmp.domain.model.Board
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class KtorBoardRepository(private val client: HttpClient) : BoardRepository {

    override suspend fun getBoards(): AppResult<List<Board>> = safeApiCall {
        client.get("/api/tasks/boards")
            .toResult<List<BoardDto>>()
            .flatMap { it.toDomainList() }
    }

    override suspend fun createBoard(board: NewBoard): AppResult<Board> = safeApiCall {
        val response = client.post("/api/tasks/boards") {
            contentType(ContentType.Application.Json)
            setBody(board.toCreateDto())
        }
        response.toResult<BoardDto>().flatMap { it.toDomain() }
    }

    override suspend fun deleteBoard(id: String): AppResult<Unit> = safeApiCall {
        client.delete("/api/tasks/boards/$id").toUnitResult()
    }
}