// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/data/repository/KtorBoardRepositoryTest.kt
package com.castrodev.task_manager_kmp.data.repository

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.core.getOrNull
import com.castrodev.task_manager_kmp.data.remote.jsonResponse
import com.castrodev.task_manager_kmp.data.remote.testClient
import com.castrodev.task_manager_kmp.domain.model.NewBoard
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
import kotlin.test.assertTrue

class KtorBoardRepositoryTest {

    @Test
    fun `getBoards maps the response and sends the bearer token`() = runTest {
        val engine = MockEngine { jsonResponse("[${boardJson(id = "1")},${boardJson(id = "2")}]") }
        val repository = KtorBoardRepository(testClient(engine))

        val boards = assertNotNull(repository.getBoards().getOrNull())

        assertEquals(listOf("1", "2"), boards.map { it.id })
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/api/tasks/boards", request.url.encodedPath)
        assertEquals("Bearer token-123", request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun `getBoards returns unauthorized for a 401 response`() = runTest {
        val engine = MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) }
        val repository = KtorBoardRepository(testClient(engine))

        val failure = assertIs<AppResult.Failure>(repository.getBoards())

        assertEquals(AppError.Unauthorized, failure.error)
    }

    @Test
    fun `createBoard posts the dto and returns the created board`() = runTest {
        val engine = MockEngine { jsonResponse(boardJson(id = "new-1"), HttpStatusCode.Created) }
        val repository = KtorBoardRepository(testClient(engine))

        val board = assertNotNull(
            repository.createBoard(NewBoard(name = "Casa", color = "#FF9800")).getOrNull(),
        )

        assertEquals("new-1", board.id)
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/tasks/boards", request.url.encodedPath)
        val body = (request.body as TextContent).text
        assertTrue(body.contains("\"name\":\"Casa\""), body)
        assertTrue(body.contains("\"color\":\"#FF9800\""), body)
    }

    @Test
    fun `deleteBoard succeeds with a 204 response`() = runTest {
        val engine = MockEngine { respond(content = "", status = HttpStatusCode.NoContent) }
        val repository = KtorBoardRepository(testClient(engine))

        val result = repository.deleteBoard("board-1")

        assertEquals(AppResult.Success(Unit), result)
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("/api/tasks/boards/board-1", request.url.encodedPath)
    }

    private fun boardJson(id: String = "board-1") = """
        {"id":"$id","name":"Casa","description":"","color":"#FF9800","taskCount":0,
         "createdAt":"2026-10-30T19:00:00.1234567Z"}
    """.trimIndent()
}