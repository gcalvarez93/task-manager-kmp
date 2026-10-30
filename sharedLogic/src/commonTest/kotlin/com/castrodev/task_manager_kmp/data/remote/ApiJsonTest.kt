// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/data/remote/ApiJsonTest.kt
package com.castrodev.task_manager_kmp.data.remote

import com.castrodev.task_manager_kmp.core.getOrNull
import com.castrodev.task_manager_kmp.data.mapper.toDomain
import com.castrodev.task_manager_kmp.data.remote.dto.CreateTaskDto
import com.castrodev.task_manager_kmp.data.remote.dto.TaskDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApiJsonTest {

    @Test
    fun `decodes a task response like the one the api sends`() {
        // Fechas con 7 decimales y con "Z", como las serializa .NET.
        val json = """
            {
              "id": "abc123",
              "boardId": "board-1",
              "title": "Comprar pan",
              "description": "",
              "status": "todo",
              "priority": "medium",
              "dueDate": "2026-11-01T10:00:00.1234567Z",
              "labels": ["casa"],
              "createdAt": "2026-10-30T19:00:00.1234567Z"
            }
        """.trimIndent()

        val dto = apiJson.decodeFromString<TaskDto>(json)
        val task = assertNotNull(dto.toDomain().getOrNull())

        assertEquals("abc123", task.id)
        assertEquals(listOf("casa"), task.labels)
    }

    @Test
    fun `decodes a list of tasks and ignores unknown fields`() {
        val json = """
            [
              {
                "id": "1", "boardId": "b", "title": "A", "description": "",
                "status": "done", "priority": "low", "dueDate": null, "labels": [],
                "createdAt": "2026-10-30T19:00:00Z", "campoNuevo": 42
              }
            ]
        """.trimIndent()

        val dtos = apiJson.decodeFromString<List<TaskDto>>(json)

        assertEquals(1, dtos.size)
        assertEquals("done", dtos.first().status)
    }

    @Test
    fun `encodes an empty labels list instead of omitting it`() {
        val dto = CreateTaskDto(
            boardId = "board-1",
            title = "Tarea",
            description = "",
            priority = "high",
            status = "todo",
        )

        val json = apiJson.encodeToString(dto)

        assertTrue(json.contains("\"labels\":[]"), "labels debe enviarse siempre: $json")
        assertTrue(json.contains("\"priority\":\"high\""), json)
    }
}