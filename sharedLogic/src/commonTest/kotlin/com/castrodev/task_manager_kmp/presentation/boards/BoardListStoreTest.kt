// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/presentation/boards/BoardListStoreTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.castrodev.task_manager_kmp.presentation.boards

import app.cash.turbine.test
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.domain.usecase.CreateBoardUseCase
import com.castrodev.task_manager_kmp.testing.FakeBoardRepository
import com.castrodev.task_manager_kmp.testing.boardItem
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

class BoardListStoreTest {

    private fun TestScope.storeWith(repository: FakeBoardRepository) = BoardListStore(
        repository = repository,
        createBoardUseCase = CreateBoardUseCase(repository),
        scope = backgroundScope,
    )

    @Test
    fun `initial state is empty and not loading`() = runTest {
        val store = storeWith(FakeBoardRepository())

        assertEquals(BoardListState(), store.state.value)
    }

    @Test
    fun `refresh emits loading and then the loaded boards`() = runTest {
        val repository = FakeBoardRepository(listOf(boardItem("1"), boardItem("2")))
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
            assertEquals(listOf("1", "2"), loaded.boards.map { it.id })
            assertNull(loaded.error)
        }
    }

    @Test
    fun `refresh failure with nothing to show exposes a load error instead of an empty list`() = runTest {
        val repository = FakeBoardRepository().apply { failWith = AppError.Unauthorized }
        val store = storeWith(repository)

        store.refresh()
        runCurrent()

        val state = store.state.value
        assertFalse(state.isLoading)
        assertEquals(AppError.Unauthorized, state.loadError)
        assertNull(state.error)
        assertTrue(state.boards.isEmpty())
    }

    @Test
    fun `refresh failure with boards on screen keeps them and exposes a transient error`() = runTest {
        val repository = FakeBoardRepository(listOf(boardItem("1")))
        val store = storeWith(repository)
        store.refresh()
        runCurrent()
        repository.failWith = AppError.Timeout

        store.refresh()
        runCurrent()

        val state = store.state.value
        assertEquals(listOf("1"), state.boards.map { it.id })
        assertEquals(AppError.Timeout, state.error)
        assertNull(state.loadError)
    }

    @Test
    fun `retrying after a load error clears it and loads the boards`() = runTest {
        val repository = FakeBoardRepository(listOf(boardItem("1"))).apply { failWith = AppError.Timeout }
        val store = storeWith(repository)
        store.refresh()
        runCurrent()
        assertEquals(AppError.Timeout, store.state.value.loadError)

        repository.failWith = null
        store.refresh()
        runCurrent()

        val state = store.state.value
        assertNull(state.loadError)
        assertEquals(listOf("1"), state.boards.map { it.id })
    }

    @Test
    fun `openCreateForm opens the form and closeCreateForm closes it`() = runTest {
        val store = storeWith(FakeBoardRepository())

        store.openCreateForm()
        assertTrue(store.state.value.isCreateFormOpen)

        store.closeCreateForm()
        assertFalse(store.state.value.isCreateFormOpen)
    }

    @Test
    fun `create adds the new board to the list and closes the form`() = runTest {
        val store = storeWith(FakeBoardRepository())
        store.openCreateForm()

        store.create(NewBoard(name = "Casa", color = "#FF9800"))
        runCurrent()

        val state = store.state.value
        assertEquals("Casa", state.boards.single().name)
        assertFalse(state.isCreateFormOpen)
        assertFalse(state.isCreating)
        assertNull(state.createError)
    }

    @Test
    fun `create with a blank name keeps the form open with a validation error`() = runTest {
        val store = storeWith(FakeBoardRepository())
        store.openCreateForm()

        store.create(NewBoard(name = " ", color = "#FF9800"))
        runCurrent()

        val state = store.state.value
        assertIs<AppError.Validation>(state.createError)
        assertTrue(state.isCreateFormOpen)
        assertFalse(state.isCreating)
        assertNull(state.error)
        assertTrue(state.boards.isEmpty())
    }

    @Test
    fun `create failure keeps the form open so the input is not lost`() = runTest {
        val repository = FakeBoardRepository().apply { failWith = AppError.Network("sin conexion") }
        val store = storeWith(repository)
        store.openCreateForm()

        store.create(NewBoard(name = "Casa", color = "#FF9800"))
        runCurrent()

        val state = store.state.value
        assertIs<AppError.Network>(state.createError)
        assertTrue(state.isCreateFormOpen)
        assertFalse(state.isCreating)
        assertNull(state.error)
    }

    @Test
    fun `create is ignored while another attempt is in progress`() = runTest {
        val repository = FakeBoardRepository()
        repository.createGate = CompletableDeferred()
        val store = storeWith(repository)
        store.openCreateForm()

        store.create(NewBoard(name = "Casa", color = "#FF9800"))
        runCurrent()
        assertTrue(store.state.value.isCreating)
        store.create(NewBoard(name = "Casa", color = "#FF9800"))
        runCurrent()
        repository.createGate?.complete(Unit)
        runCurrent()

        assertEquals(1, repository.created.size)
        assertEquals(1, store.state.value.boards.size)
    }

    @Test
    fun `closeCreateForm is ignored while a create is in progress`() = runTest {
        val repository = FakeBoardRepository()
        repository.createGate = CompletableDeferred()
        val store = storeWith(repository)
        store.openCreateForm()
        store.create(NewBoard(name = "Casa", color = "#FF9800"))
        runCurrent()

        store.closeCreateForm()

        assertTrue(store.state.value.isCreateFormOpen)
    }

    @Test
    fun `delete removes the board from the list`() = runTest {
        val store = storeWith(FakeBoardRepository(listOf(boardItem("1"), boardItem("2"))))
        store.refresh()
        runCurrent()

        store.delete("1")
        runCurrent()

        assertEquals(listOf("2"), store.state.value.boards.map { it.id })
    }

    @Test
    fun `delete failure keeps the list and exposes the error`() = runTest {
        val repository = FakeBoardRepository(listOf(boardItem("1")))
        val store = storeWith(repository)
        store.refresh()
        runCurrent()
        repository.failWith = AppError.Network("sin conexion")

        store.delete("1")
        runCurrent()

        val state = store.state.value
        assertEquals(1, state.boards.size)
        assertIs<AppError.Network>(state.error)
    }

    @Test
    fun `clearError removes the pending error`() = runTest {
        val repository = FakeBoardRepository(listOf(boardItem("1")))
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