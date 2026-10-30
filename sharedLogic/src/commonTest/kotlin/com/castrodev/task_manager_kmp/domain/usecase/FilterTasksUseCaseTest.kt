// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/domain/usecase/FilterTasksUseCaseTest.kt
package com.castrodev.task_manager_kmp.domain.usecase

import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskPriority
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class FilterTasksUseCaseTest {

    private val useCase = FilterTasksUseCase()

    @Test
    fun `returns every task when no status is given`() {
        val tasks = listOf(
            task(id = "1", status = TaskStatus.PENDING),
            task(id = "2", status = TaskStatus.DONE),
        )

        val result = useCase(tasks, status = null)

        assertEquals(setOf("1", "2"), result.map { it.id }.toSet())
    }

    @Test
    fun `keeps only the tasks with the given status`() {
        val tasks = listOf(
            task(id = "1", status = TaskStatus.PENDING),
            task(id = "2", status = TaskStatus.DONE),
            task(id = "3", status = TaskStatus.DONE),
        )

        val result = useCase(tasks, status = TaskStatus.DONE)

        assertEquals(listOf("2", "3"), result.map { it.id })
    }

    @Test
    fun `sorts by priority from high to low`() {
        val tasks = listOf(
            task(id = "low", priority = TaskPriority.LOW),
            task(id = "high", priority = TaskPriority.HIGH),
            task(id = "medium", priority = TaskPriority.MEDIUM),
        )

        val result = useCase(tasks, status = null)

        assertEquals(listOf("high", "medium", "low"), result.map { it.id })
    }

    @Test
    fun `sorts by due date within the same priority and puts tasks without due date last`() {
        val tasks = listOf(
            task(id = "none", dueDate = null),
            task(id = "later", dueDate = Instant.fromEpochSeconds(2_000)),
            task(id = "sooner", dueDate = Instant.fromEpochSeconds(1_000)),
        )

        val result = useCase(tasks, status = null)

        assertEquals(listOf("sooner", "later", "none"), result.map { it.id })
    }

    @Test
    fun `returns an empty list when there are no tasks`() {
        assertEquals(emptyList<TaskItem>(), useCase(emptyList(), status = TaskStatus.PENDING))
    }

    private fun task(
        id: String,
        status: TaskStatus = TaskStatus.PENDING,
        priority: TaskPriority = TaskPriority.MEDIUM,
        dueDate: Instant? = null,
    ) = TaskItem(
        id = id,
        boardId = "board-1",
        title = "Tarea $id",
        description = "",
        status = status,
        priority = priority,
        dueDate = dueDate,
        labels = emptyList(),
        createdAt = Instant.fromEpochSeconds(1_700_000_000),
    )
}