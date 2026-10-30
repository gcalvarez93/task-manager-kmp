// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/presentation/tasks/TaskListStoreTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.castrodev.task_manager_kmp.presentation.tasks

import app.cash.turbine.test
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskPriority
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import com.castrodev.task_manager_kmp.domain.usecase.CreateTaskUseCase
import com.castrodev.task_manager_kmp.domain.usecase.FilterTasksUseCase
import com.castrodev.task_manager_kmp.testing.FakeTaskRepository
import com.castrodev.task_manager_kmp.testing.taskItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskListStoreTest {

    private fun TestScope.storeWith(repository: FakeTaskRepository) = TaskListStore(
        boardId = "board-1",
        repository = repository,
        createTaskUseCase = CreateTaskUseCase(repository),
        filterTasks = FilterTasksUseCase(),
        scope = backgroundScope,
    )

    @Test
    fun `initial state is empty and not loading`() = runTest {
        val store = storeWith(FakeTaskRepository())

        assertEquals(TaskListState(), store.state.value)
    }

    @Test
    fun `refresh emits loading and then the loaded tasks`() = runTest {
        val repository = FakeTaskRepository(listOf(taskItem("1"), taskItem("2")))
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val store = storeWith(repository)

        store.state.test {
            assertFalse(awaitItem().isLoading)

            store.refresh()
            assertTrue(awaitItem().isLoading)

            gate.complete(Unit)
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(listOf("1", "2"), loaded.allTasks.map { it.id }.sorted())
            assertNull(loaded.error)
        }
    }

    @Test
    fun `refresh failure stops loading and exposes the error`() = runTest {
        val repository = FakeTaskRepository().apply { failWith = AppError.Unauthorized }
        val store = storeWith(repository)

        store.refresh()
        runCurrent()

        val state = store.state.value
        assertFalse(state.isLoading)
        assertEquals(AppError.Unauthorized, state.error)
        assertTrue(state.allTasks.isEmpty())
    }

    @Test
    fun `visible tasks are sorted by priority`() = runTest {
        val repository = FakeTaskRepository(
            listOf(
                taskItem("low", priority = TaskPriority.LOW),
                taskItem("high", priority = TaskPriority.HIGH),
            ),
        )
        val store = storeWith(repository)

        store.refresh()
        runCurrent()

        assertEquals(listOf("high", "low"), store.state.value.visibleTasks.map { it.id })
    }

    @Test
    fun `setFilter shows only tasks with that status`() = runTest {
        val repository = FakeTaskRepository(
            listOf(
                taskItem("1", status = TaskStatus.PENDING),
                taskItem("2", status = TaskStatus.DONE),
            ),
        )
        val store = storeWith(repository)
        store.refresh()
        runCurrent()

        store.setFilter(TaskStatus.DONE)

        val state = store.state.value
        assertEquals(TaskStatus.DONE, state.filter)
        assertEquals(listOf("2"), state.visibleTasks.map { it.id })
        assertEquals(2, state.allTasks.size)
    }

    @Test
    fun `setFilter null shows all tasks again`() = runTest {
        val repository = FakeTaskRepository(
            listOf(
                taskItem("1", status = TaskStatus.PENDING),
                taskItem("2", status = TaskStatus.DONE),
            ),
        )
        val store = storeWith(repository)
        store.refresh()
        runCurrent()
        store.setFilter(TaskStatus.DONE)

        store.setFilter(null)

        assertEquals(2, store.state.value.visibleTasks.size)
    }

    @Test
    fun `changeStatus replaces the task in the list`() = runTest {
        val task = taskItem("1", status = TaskStatus.PENDING)
        val store = storeWith(FakeTaskRepository(listOf(task)))
        store.refresh()
        runCurrent()

        store.changeStatus(task, TaskStatus.DONE)
        runCurrent()

        assertEquals(TaskStatus.DONE, store.state.value.allTasks.single().status)
    }

    @Test
    fun `changeStatus failure keeps the list and exposes the error`() = runTest {
        val task = taskItem("1", status = TaskStatus.PENDING)
        val repository = FakeTaskRepository(listOf(task))
        val store = storeWith(repository)
        store.refresh()
        runCurrent()
        repository.failWith = AppError.Network("sin conexion")

        store.changeStatus(task, TaskStatus.DONE)
        runCurrent()

        val state = store.state.value
        assertEquals(TaskStatus.PENDING, state.allTasks.single().status)
        assertIs<AppError.Network>(state.error)
    }

    @Test
    fun `delete removes the task from the list`() = runTest {
        val store = storeWith(FakeTaskRepository(listOf(taskItem("1"), taskItem("2"))))
        store.refresh()
        runCurrent()

        store.delete("1")
        runCurrent()

        assertEquals(listOf("2"), store.state.value.allTasks.map { it.id })
    }

    @Test
    fun `create adds the new task to the list`() = runTest {
        val store = storeWith(FakeTaskRepository())

        store.create(NewTask(boardId = "board-1", title = "Comprar pan"))
        runCurrent()

        assertEquals("Comprar pan", store.state.value.allTasks.single().title)
    }

    @Test
    fun `create with a blank title exposes a validation error`() = runTest {
        val store = storeWith(FakeTaskRepository())

        store.create(NewTask(boardId = "board-1", title = " "))
        runCurrent()

        val state = store.state.value
        assertIs<AppError.Validation>(state.error)
        assertTrue(state.allTasks.isEmpty())
    }

    @Test
    fun `clearError removes the pending error`() = runTest {
        val store = storeWith(FakeTaskRepository().apply { failWith = AppError.Unauthorized })
        store.refresh()
        runCurrent()

        store.clearError()

        assertNull(store.state.value.error)
    }
}