// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/data/mapper/TaskMapperTest.kt
package com.castrodev.task_manager_kmp.data.mapper

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.core.getOrNull
import com.castrodev.task_manager_kmp.data.remote.dto.TaskDto
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskPriority
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

class TaskMapperTest {

    @Test
    fun `maps a valid dto to a domain task`() {
        val dto = taskDto(
            status = "inProgress",
            priority = "high",
            dueDate = "2026-11-01T10:00:00Z",
            labels = listOf("urgente", "casa"),
        )

        val task = assertNotNull(dto.toDomain().getOrNull())

        assertEquals("task-1", task.id)
        assertEquals("board-1", task.boardId)
        assertEquals("Comprar pan", task.title)
        assertEquals(TaskStatus.IN_PROGRESS, task.status)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertEquals(Instant.parse("2026-11-01T10:00:00Z"), task.dueDate)
        assertEquals(Instant.parse("2026-10-30T19:00:00Z"), task.createdAt)
        assertEquals(listOf("urgente", "casa"), task.labels)
    }

    @Test
    fun `maps a dto without due date`() {
        val task = assertNotNull(taskDto(dueDate = null).toDomain().getOrNull())

        assertNull(task.dueDate)
    }

    @Test
    fun `fails with invalid data for an unknown status`() {
        val result = taskDto(status = "archived").toDomain()

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `fails with invalid data for an unknown priority`() {
        val result = taskDto(priority = "urgent").toDomain()

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `fails with invalid data for a malformed creation date`() {
        val result = taskDto(createdAt = "ayer").toDomain()

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `fails with invalid data for a malformed due date`() {
        val result = taskDto(dueDate = "pronto").toDomain()

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `maps a whole list when every task is valid`() {
        val dtos = listOf(taskDto(id = "1"), taskDto(id = "2"))

        val tasks = assertNotNull(dtos.toDomainList().getOrNull())

        assertEquals(listOf("1", "2"), tasks.map { it.id })
    }

    @Test
    fun `fails the whole list when one task is invalid`() {
        val dtos = listOf(taskDto(id = "1"), taskDto(id = "2", status = "archived"))

        val result = dtos.toDomainList()

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.InvalidData>(failure.error)
    }

    @Test
    fun `builds the create dto with api values and an iso due date`() {
        val newTask = NewTask(
            boardId = "board-1",
            title = "Pagar el alquiler",
            priority = TaskPriority.HIGH,
            status = TaskStatus.PENDING,
            dueDate = Instant.parse("2026-11-01T10:00:00Z"),
        )

        val dto = newTask.toCreateDto()

        assertEquals("board-1", dto.boardId)
        assertEquals("high", dto.priority)
        assertEquals("todo", dto.status)
        assertEquals("2026-11-01T10:00:00Z", dto.dueDate)
        assertEquals(emptyList<String>(), dto.labels)
    }

    @Test
    fun `builds the update dto with every field of the task`() {
        val task = TaskItem(
            id = "task-1",
            boardId = "board-1",
            title = "Llamar al banco",
            description = "Antes de las 14:00",
            status = TaskStatus.DONE,
            priority = TaskPriority.LOW,
            dueDate = null,
            labels = listOf("llamadas"),
            createdAt = Instant.parse("2026-10-30T19:00:00Z"),
        )

        val dto = task.toUpdateDto()

        assertEquals("Llamar al banco", dto.title)
        assertEquals("Antes de las 14:00", dto.description)
        assertEquals("done", dto.status)
        assertEquals("low", dto.priority)
        assertNull(dto.dueDate)
        assertEquals(listOf("llamadas"), dto.labels)
    }

    private fun taskDto(
        id: String = "task-1",
        status: String = "todo",
        priority: String = "medium",
        dueDate: String? = null,
        labels: List<String> = emptyList(),
        createdAt: String = "2026-10-30T19:00:00Z",
    ) = TaskDto(
        id = id,
        boardId = "board-1",
        title = "Comprar pan",
        description = "",
        status = status,
        priority = priority,
        dueDate = dueDate,
        labels = labels,
        createdAt = createdAt,
    )
}