// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/data/repository/KtorTaskRepositoryTest.kt
package com.castrodev.task_manager_kmp.data.repository

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.core.getOrNull
import com.castrodev.task_manager_kmp.data.remote.FakeTokenProvider
import com.castrodev.task_manager_kmp.data.remote.jsonResponse
import com.castrodev.task_manager_kmp.data.remote.testClient
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskPriority
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class KtorTaskRepositoryTest {

    @Test
    fun `getTasks maps the response and sends the bearer token`() = runTest {
        val engine = MockEngine { jsonResponse("[${taskJson(id = "1")},${taskJson(id = "2")}]") }
        val repository = KtorTaskRepository(testClient(engine))

        val tasks = assertNotNull(repository.getTasks().getOrNull())

        assertEquals(listOf("1", "2"), tasks.map { it.id })
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/api/tasks", request.url.encodedPath)
        assertEquals("Bearer token-123", request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun `getTasks filters by board when a board id is given`() = runTest {
        val engine = MockEngine { jsonResponse("[]") }
        val repository = KtorTaskRepository(testClient(engine))

        repository.getTasks(boardId = "board-1")

        assertEquals("board-1", engine.requestHistory.single().url.parameters["boardId"])
    }

    @Test
    fun `requests are sent without authorization when there is no session`() = runTest {
        val engine = MockEngine { jsonResponse("[]") }
        val repository = KtorTaskRepository(testClient(engine, FakeTokenProvider(token = null)))

        repository.getTasks()

        assertNull(engine.requestHistory.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `getTasks returns unauthorized for a 401 response`() = runTest {
        val engine = MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) }
        val repository = KtorTaskRepository(testClient(engine))

        val failure = assertIs<AppResult.Failure>(repository.getTasks())

        assertEquals(AppError.Unauthorized, failure.error)
    }

    @Test
    fun `getTasks returns not found for a 404 response`() = runTest {
        val engine = MockEngine { respond(content = "", status = HttpStatusCode.NotFound) }
        val repository = KtorTaskRepository(testClient(engine))

        val failure = assertIs<AppResult.Failure>(repository.getTasks())

        assertEquals(AppError.NotFound, failure.error)
    }

    @Test
    fun `getTasks returns a validation error for a 400 response`() = runTest {
        val engine = MockEngine { jsonResponse("titulo vacio", HttpStatusCode.BadRequest) }
        val repository = KtorTaskRepository(testClient(engine))

        val failure = assertIs<AppResult.Failure>(repository.getTasks())

        val error = assertIs<AppError.Validation>(failure.error)
        assertTrue(error.message.contains("titulo vacio"), error.message)
    }

    @Test
    fun `getTasks returns a server error with the status code for a 500 response`() = runTest {
        val engine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }
        val repository = KtorTaskRepository(testClient(engine))

        val failure = assertIs<AppResult.Failure>(repository.getTasks())

        val error = assertIs<AppError.Server>(failure.error)
        assertEquals(500, error.statusCode)
    }

    @Test
    fun `getTasks returns a network error when the request fails`() = runTest {
        val engine = MockEngine { throw RuntimeException("sin conexion") }
        val repository = KtorTaskRepository(testClient(engine))

        val failure = assertIs<AppResult.Failure>(repository.getTasks())

        assertIs<AppError.Network>(failure.error)
    }

    @Test
    fun `getTasks returns invalid data when the body does not have the expected shape`() = runTest {
        val engine = MockEngine { jsonResponse("""{"inesperado": true}""") }
        val repository = KtorTaskRepository(testClient(engine))

        val failure = assertIs<AppResult.Failure>(repository.getTasks())

        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `getTasks returns invalid data when a task has an unknown status`() = runTest {
        val engine = MockEngine { jsonResponse("[${taskJson(status = "archived")}]") }
        val repository = KtorTaskRepository(testClient(engine))

        val failure = assertIs<AppResult.Failure>(repository.getTasks())

        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `createTask posts the dto and returns the created task`() = runTest {
        val engine = MockEngine { jsonResponse(taskJson(id = "new-1"), HttpStatusCode.Created) }
        val repository = KtorTaskRepository(testClient(engine))

        val task = assertNotNull(
            repository.createTask(
                NewTask(boardId = "board-1", title = "Comprar pan", priority = TaskPriority.HIGH),
            ).getOrNull(),
        )

        assertEquals("new-1", task.id)
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/tasks", request.url.encodedPath)
        val body = (request.body as TextContent).text
        assertTrue(body.contains("\"title\":\"Comprar pan\""), body)
        assertTrue(body.contains("\"priority\":\"high\""), body)
        assertTrue(body.contains("\"labels\":[]"), body)
    }

    @Test
    fun `updateTask puts the whole task to its url`() = runTest {
        val engine = MockEngine { jsonResponse(taskJson(id = "task-1", status = "done")) }
        val repository = KtorTaskRepository(testClient(engine))
        val task = TaskItem(
            id = "task-1",
            boardId = "board-1",
            title = "Comprar pan",
            description = "",
            status = TaskStatus.DONE,
            priority = TaskPriority.MEDIUM,
            dueDate = null,
            labels = emptyList(),
            createdAt = Instant.parse("2026-10-30T19:00:00Z"),
        )

        val updated = assertNotNull(repository.updateTask(task).getOrNull())

        assertEquals(TaskStatus.DONE, updated.status)
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Put, request.method)
        assertEquals("/api/tasks/task-1", request.url.encodedPath)
        val body = (request.body as TextContent).text
        assertTrue(body.contains("\"status\":\"done\""), body)
    }

    @Test
    fun `deleteTask succeeds with a 204 response`() = runTest {
        val engine = MockEngine { respond(content = "", status = HttpStatusCode.NoContent) }
        val repository = KtorTaskRepository(testClient(engine))

        val result = repository.deleteTask("task-1")

        assertEquals(AppResult.Success(Unit), result)
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("/api/tasks/task-1", request.url.encodedPath)
    }

    private fun taskJson(id: String = "task-1", status: String = "todo") = """
        {"id":"$id","boardId":"board-1","title":"Comprar pan","description":"","status":"$status",
         "priority":"medium","dueDate":null,"labels":[],"createdAt":"2026-10-30T19:00:00.1234567Z"}
    """.trimIndent()
}