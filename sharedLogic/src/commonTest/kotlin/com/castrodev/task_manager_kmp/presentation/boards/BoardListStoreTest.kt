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
    fun `refresh failure stops loading and exposes the error`() = runTest {
        val repository = FakeBoardRepository().apply { failWith = AppError.Unauthorized }
        val store = storeWith(repository)

        store.refresh()
        runCurrent()

        val state = store.state.value
        assertFalse(state.isLoading)
        assertEquals(AppError.Unauthorized, state.error)
        assertTrue(state.boards.isEmpty())
    }

    @Test
    fun `create adds the new board to the list`() = runTest {
        val store = storeWith(FakeBoardRepository())

        store.create(NewBoard(name = "Casa", color = "#FF9800"))
        runCurrent()

        assertEquals("Casa", store.state.value.boards.single().name)
    }

    @Test
    fun `create with a blank name exposes a validation error`() = runTest {
        val store = storeWith(FakeBoardRepository())

        store.create(NewBoard(name = " ", color = "#FF9800"))
        runCurrent()

        val state = store.state.value
        assertIs<AppError.Validation>(state.error)
        assertTrue(state.boards.isEmpty())
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
        val store = storeWith(FakeBoardRepository().apply { failWith = AppError.Unauthorized })
        store.refresh()
        runCurrent()

        store.clearError()

        assertNull(store.state.value.error)
    }
}