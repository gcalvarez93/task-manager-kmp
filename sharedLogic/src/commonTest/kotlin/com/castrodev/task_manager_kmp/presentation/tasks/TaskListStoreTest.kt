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
    fun `refresh failure with nothing to show exposes a load error instead of an empty list`() = runTest {
        val repository = FakeTaskRepository().apply { failWith = AppError.Unauthorized }
        val store = storeWith(repository)

        store.refresh()
        runCurrent()

        val state = store.state.value
        assertFalse(state.isLoading)
        assertEquals(AppError.Unauthorized, state.loadError)
        assertNull(state.error)
        assertTrue(state.allTasks.isEmpty())
    }

    @Test
    fun `refresh failure with tasks on screen keeps them and exposes a transient error`() = runTest {
        val repository = FakeTaskRepository(listOf(taskItem("1")))
        val store = storeWith(repository)
        store.refresh()
        runCurrent()
        repository.failWith = AppError.Timeout

        store.refresh()
        runCurrent()

        val state = store.state.value
        assertEquals(listOf("1"), state.allTasks.map { it.id })
        assertEquals(AppError.Timeout, state.error)
        assertNull(state.loadError)
    }

    @Test
    fun `retrying after a load error clears it and loads the tasks`() = runTest {
        val repository = FakeTaskRepository(listOf(taskItem("1"))).apply { failWith = AppError.Timeout }
        val store = storeWith(repository)
        store.refresh()
        runCurrent()
        assertEquals(AppError.Timeout, store.state.value.loadError)

        repository.failWith = null
        store.refresh()
        runCurrent()

        val state = store.state.value
        assertNull(state.loadError)
        assertEquals(listOf("1"), state.allTasks.map { it.id })
    }

    @Test
    fun `refresh is ignored while a load is already in progress`() = runTest {
        val repository = FakeTaskRepository(listOf(taskItem("1")))
        repository.gate = CompletableDeferred()
        val store = storeWith(repository)

        store.refresh()
        runCurrent()
        store.refresh()
        runCurrent()
        repository.gate?.complete(Unit)
        runCurrent()

        assertEquals(listOf("1"), store.state.value.allTasks.map { it.id })
        assertFalse(store.state.value.isLoading)
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
    fun `openCreateForm opens the form and closeCreateForm closes it`() = runTest {
        val store = storeWith(FakeTaskRepository())

        store.openCreateForm()
        assertTrue(store.state.value.isCreateFormOpen)

        store.closeCreateForm()
        assertFalse(store.state.value.isCreateFormOpen)
    }

    @Test
    fun `create adds the new task to the list and closes the form`() = runTest {
        val store = storeWith(FakeTaskRepository())
        store.openCreateForm()

        store.create(NewTask(boardId = "board-1", title = "Comprar pan"))
        runCurrent()

        val state = store.state.value
        assertEquals("Comprar pan", state.allTasks.single().title)
        assertFalse(state.isCreateFormOpen)
        assertFalse(state.isCreating)
        assertNull(state.createError)
    }

    @Test
    fun `create with a blank title keeps the form open with a validation error`() = runTest {
        val store = storeWith(FakeTaskRepository())
        store.openCreateForm()

        store.create(NewTask(boardId = "board-1", title = " "))
        runCurrent()

        val state = store.state.value
        assertIs<AppError.Validation>(state.createError)
        assertTrue(state.isCreateFormOpen)
        assertFalse(state.isCreating)
        assertNull(state.error)
        assertTrue(state.allTasks.isEmpty())
    }

    @Test
    fun `create failure keeps the form open so the input is not lost`() = runTest {
        val repository = FakeTaskRepository().apply { failWith = AppError.Network("sin conexion") }
        val store = storeWith(repository)
        store.openCreateForm()

        store.create(NewTask(boardId = "board-1", title = "Comprar pan"))
        runCurrent()

        val state = store.state.value
        assertIs<AppError.Network>(state.createError)
        assertTrue(state.isCreateFormOpen)
        assertFalse(state.isCreating)
        assertNull(state.error)
    }

    @Test
    fun `a new attempt clears the previous create error`() = runTest {
        val repository = FakeTaskRepository().apply { failWith = AppError.Timeout }
        val store = storeWith(repository)
        store.openCreateForm()
        store.create(NewTask(boardId = "board-1", title = "Comprar pan"))
        runCurrent()
        assertEquals(AppError.Timeout, store.state.value.createError)

        repository.failWith = null
        store.create(NewTask(boardId = "board-1", title = "Comprar pan"))
        runCurrent()

        val state = store.state.value
        assertNull(state.createError)
        assertFalse(state.isCreateFormOpen)
        assertEquals(1, state.allTasks.size)
    }

    @Test
    fun `create is ignored while another attempt is in progress`() = runTest {
        val repository = FakeTaskRepository()
        repository.createGate = CompletableDeferred()
        val store = storeWith(repository)
        store.openCreateForm()

        store.create(NewTask(boardId = "board-1", title = "Comprar pan"))
        runCurrent()
        assertTrue(store.state.value.isCreating)
        store.create(NewTask(boardId = "board-1", title = "Comprar pan"))
        runCurrent()
        repository.createGate?.complete(Unit)
        runCurrent()

        assertEquals(1, repository.created.size)
        assertEquals(1, store.state.value.allTasks.size)
    }

    @Test
    fun `closeCreateForm is ignored while a create is in progress`() = runTest {
        val repository = FakeTaskRepository()
        repository.createGate = CompletableDeferred()
        val store = storeWith(repository)
        store.openCreateForm()
        store.create(NewTask(boardId = "board-1", title = "Comprar pan"))
        runCurrent()

        store.closeCreateForm()

        assertTrue(store.state.value.isCreateFormOpen)
    }

    @Test
    fun `closeCreateForm discards the create error`() = runTest {
        val store = storeWith(FakeTaskRepository())
        store.openCreateForm()
        store.create(NewTask(boardId = "board-1", title = " "))
        runCurrent()

        store.closeCreateForm()

        val state = store.state.value
        assertFalse(state.isCreateFormOpen)
        assertNull(state.createError)
    }

    @Test
    fun `clearError removes the pending error`() = runTest {
        val task = taskItem("1")
        val repository = FakeTaskRepository(listOf(task))
        val store = storeWith(repository)
        store.refresh()
        runCurrent()
        repository.failWith = AppError.Unauthorized
        store.delete("1")
        runCurrent()
        assertEquals(AppError.Unauthorized, store.state.value.error)

        store.clearError()

        assertNull(store.state.value.error)
    }
}