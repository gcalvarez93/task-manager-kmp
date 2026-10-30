// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/data/mapper/BoardMapperTest.kt
package com.castrodev.task_manager_kmp.data.mapper

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.core.getOrNull
import com.castrodev.task_manager_kmp.data.remote.dto.BoardDto
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Instant

class BoardMapperTest {

    @Test
    fun `maps a valid dto to a domain board`() {
        val board = assertNotNull(boardDto().toDomain().getOrNull())

        assertEquals("board-1", board.id)
        assertEquals("Casa", board.name)
        assertEquals("#FF9800", board.color)
        assertEquals(3, board.taskCount)
        assertEquals(Instant.parse("2026-10-30T19:00:00Z"), board.createdAt)
    }

    @Test
    fun `fails with invalid data for a malformed creation date`() {
        val result = boardDto(createdAt = "ayer").toDomain()

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `maps a whole list when every board is valid`() {
        val dtos = listOf(boardDto(id = "1"), boardDto(id = "2"))

        val boards = assertNotNull(dtos.toDomainList().getOrNull())

        assertEquals(listOf("1", "2"), boards.map { it.id })
    }

    @Test
    fun `fails the whole list when one board is invalid`() {
        val dtos = listOf(boardDto(id = "1"), boardDto(id = "2", createdAt = "ayer"))

        val result = dtos.toDomainList()

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `builds the create dto from a new board`() {
        val dto = NewBoard(name = "Trabajo", color = "#2196F3").toCreateDto()

        assertEquals("Trabajo", dto.name)
        assertEquals("", dto.description)
        assertEquals("#2196F3", dto.color)
    }

    private fun boardDto(
        id: String = "board-1",
        createdAt: String = "2026-10-30T19:00:00Z",
    ) = BoardDto(
        id = id,
        name = "Casa",
        description = "Tareas del hogar",
        color = "#FF9800",
        taskCount = 3,
        createdAt = createdAt,
    )
}