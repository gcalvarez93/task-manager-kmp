// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/repository/KtorTaskRepository.kt
package com.castrodev.task_manager_kmp.data.repository

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.core.flatMap
import com.castrodev.task_manager_kmp.data.mapper.toCreateDto
import com.castrodev.task_manager_kmp.data.mapper.toDomain
import com.castrodev.task_manager_kmp.data.mapper.toDomainList
import com.castrodev.task_manager_kmp.data.mapper.toUpdateDto
import com.castrodev.task_manager_kmp.data.remote.dto.TaskDto
import com.castrodev.task_manager_kmp.data.remote.safeApiCall
import com.castrodev.task_manager_kmp.data.remote.toResult
import com.castrodev.task_manager_kmp.data.remote.toUnitResult
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class KtorTaskRepository(private val client: HttpClient) : TaskRepository {

    override suspend fun getTasks(boardId: String?): AppResult<List<TaskItem>> = safeApiCall {
        val response = client.get("/api/tasks") {
            if (boardId != null) {
                parameter("boardId", boardId)
            }
        }
        response.toResult<List<TaskDto>>().flatMap { it.toDomainList() }
    }

    override suspend fun createTask(task: NewTask): AppResult<TaskItem> = safeApiCall {
        val response = client.post("/api/tasks") {
            contentType(ContentType.Application.Json)
            setBody(task.toCreateDto())
        }
        response.toResult<TaskDto>().flatMap { it.toDomain() }
    }

    override suspend fun updateTask(task: TaskItem): AppResult<TaskItem> = safeApiCall {
        val response = client.put("/api/tasks/${task.id}") {
            contentType(ContentType.Application.Json)
            setBody(task.toUpdateDto())
        }
        response.toResult<TaskDto>().flatMap { it.toDomain() }
    }

    override suspend fun deleteTask(id: String): AppResult<Unit> = safeApiCall {
        client.delete("/api/tasks/$id").toUnitResult()
    }
}