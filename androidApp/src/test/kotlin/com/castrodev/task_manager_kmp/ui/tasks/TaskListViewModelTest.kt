// Path: androidApp/src/test/kotlin/com/castrodev/task_manager_kmp/ui/tasks/TaskListViewModelTest.kt
package com.castrodev.task_manager_kmp.ui.tasks

import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskPriority
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository
import com.castrodev.task_manager_kmp.domain.usecase.CreateTaskUseCase
import com.castrodev.task_manager_kmp.domain.usecase.FilterTasksUseCase
import com.castrodev.task_manager_kmp.presentation.tasks.TaskListStore
import com.castrodev.task_manager_kmp.ui.testing.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Instant

@ExtendWith(MainDispatcherExtension::class)
class TaskListViewModelTest {

    private val boardId = "board-1"
    private val repository = mockk<TaskRepository>()

    private fun task(
        id: String,
        status: TaskStatus = TaskStatus.PENDING,
        priority: TaskPriority = TaskPriority.MEDIUM,
    ) = TaskItem(
        id = id,
        boardId = boardId,
        title = "Tarea $id",
        description = "",
        status = status,
        priority = priority,
        dueDate = null,
        labels = emptyList(),
        createdAt = Instant.fromEpochMilliseconds(0),
    )

    /** El ViewModel pide el store al contenedor: aqui se lo damos real, pero con un repositorio simulado. */
    private fun viewModel(): TaskListViewModel {
        val container = mockk<AppContainer>()
        every { container.createTaskListStore(any(), any()) } answers {
            TaskListStore(
                boardId = firstArg<String?>(),
                repository = repository,
                createTaskUseCase = CreateTaskUseCase(repository),
                filterTasks = FilterTasksUseCase(),
                scope = secondArg<CoroutineScope>(),
            )
        }
        return TaskListViewModel(container, boardId)
    }

    @Test
    fun `loads the tasks of its board as soon as it is created`() {
        coEvery { repository.getTasks(boardId) } returns AppResult.Success(listOf(task("1"), task("2")))

        val viewModel = viewModel()

        assertEquals(setOf("1", "2"), viewModel.state.value.visibleTasks.map { it.id }.toSet())
        coVerify(exactly = 1) { repository.getTasks(boardId) }
    }

    @Test
    fun `exposes the error when loading fails`() {
        coEvery { repository.getTasks(boardId) } returns AppResult.Failure(AppError.Unauthorized)

        val viewModel = viewModel()

        assertEquals(AppError.Unauthorized, viewModel.state.value.error)
    }

    @Test
    fun `setFilter only shows the tasks with that status`() {
        coEvery { repository.getTasks(boardId) } returns AppResult.Success(
            listOf(task("1", TaskStatus.PENDING), task("2", TaskStatus.DONE)),
        )
        val viewModel = viewModel()

        viewModel.setFilter(TaskStatus.DONE)

        assertEquals(listOf("2"), viewModel.state.value.visibleTasks.map { it.id })
        assertEquals(2, viewModel.state.value.allTasks.size)
    }

    @Test
    fun `changeStatus updates the task through the repository`() {
        val pending = task("1", TaskStatus.PENDING)
        coEvery { repository.getTasks(boardId) } returns AppResult.Success(listOf(pending))
        coEvery { repository.updateTask(any()) } answers { AppResult.Success(firstArg<TaskItem>()) }
        val viewModel = viewModel()

        viewModel.changeStatus(pending, TaskStatus.DONE)

        coVerify { repository.updateTask(match { it.id == "1" && it.status == TaskStatus.DONE }) }
        assertEquals(TaskStatus.DONE, viewModel.state.value.allTasks.single().status)
    }

    @Test
    fun `create adds the new task to the list`() {
        coEvery { repository.getTasks(boardId) } returns AppResult.Success(emptyList())
        coEvery { repository.createTask(any()) } returns AppResult.Success(task("new"))
        val viewModel = viewModel()

        viewModel.create(NewTask(boardId = boardId, title = "Comprar pan"))

        assertEquals(listOf("new"), viewModel.state.value.allTasks.map { it.id })
    }

    @Test
    fun `create with a blank title exposes a validation error and does not call the repository`() {
        coEvery { repository.getTasks(boardId) } returns AppResult.Success(emptyList())
        val viewModel = viewModel()

        viewModel.create(NewTask(boardId = boardId, title = "  "))

        assertTrue(viewModel.state.value.error is AppError.Validation)
        coVerify(exactly = 0) { repository.createTask(any()) }
    }

    @Test
    fun `delete removes the task from the list`() {
        coEvery { repository.getTasks(boardId) } returns AppResult.Success(listOf(task("1"), task("2")))
        coEvery { repository.deleteTask("1") } returns AppResult.Success(Unit)
        val viewModel = viewModel()

        viewModel.delete("1")

        assertEquals(listOf("2"), viewModel.state.value.allTasks.map { it.id })
    }

    @Test
    fun `clearError removes the pending error`() {
        coEvery { repository.getTasks(boardId) } returns AppResult.Failure(AppError.NotFound)
        val viewModel = viewModel()

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }
}