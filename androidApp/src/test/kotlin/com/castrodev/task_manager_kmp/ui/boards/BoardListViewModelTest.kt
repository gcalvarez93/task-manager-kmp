// Path: androidApp/src/test/kotlin/com/castrodev/task_manager_kmp/ui/boards/BoardListViewModelTest.kt
package com.castrodev.task_manager_kmp.ui.boards

import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.Board
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository
import com.castrodev.task_manager_kmp.domain.usecase.CreateBoardUseCase
import com.castrodev.task_manager_kmp.presentation.boards.BoardListStore
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
class BoardListViewModelTest {

    private val repository = mockk<BoardRepository>()

    private fun board(id: String, name: String = "Tablero $id") = Board(
        id = id,
        name = name,
        description = "",
        color = "#FF9800",
        taskCount = 0,
        createdAt = Instant.fromEpochMilliseconds(0),
    )

    /** El ViewModel pide el store al contenedor: aqui se lo damos real, pero con un repositorio simulado. */
    private fun viewModel(): BoardListViewModel {
        val container = mockk<AppContainer>()
        every { container.createBoardListStore(any()) } answers {
            BoardListStore(
                repository = repository,
                createBoardUseCase = CreateBoardUseCase(repository),
                scope = firstArg<CoroutineScope>(),
            )
        }
        return BoardListViewModel(container)
    }

    @Test
    fun `loads the boards as soon as it is created`() {
        coEvery { repository.getBoards() } returns AppResult.Success(listOf(board("1"), board("2")))

        val viewModel = viewModel()

        assertEquals(listOf("1", "2"), viewModel.state.value.boards.map { it.id })
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `exposes the error when loading fails`() {
        coEvery { repository.getBoards() } returns AppResult.Failure(AppError.Network("sin conexion"))

        val viewModel = viewModel()

        assertTrue(viewModel.state.value.error is AppError.Network)
        assertTrue(viewModel.state.value.boards.isEmpty())
    }

    @Test
    fun `create adds the new board to the list`() {
        coEvery { repository.getBoards() } returns AppResult.Success(emptyList())
        coEvery { repository.createBoard(any()) } returns AppResult.Success(board("new", "Casa"))
        val viewModel = viewModel()

        viewModel.create(NewBoard(name = "Casa", color = "#FF9800"))

        assertEquals(listOf("new"), viewModel.state.value.boards.map { it.id })
    }

    @Test
    fun `create with a blank name exposes a validation error and does not call the repository`() {
        coEvery { repository.getBoards() } returns AppResult.Success(emptyList())
        val viewModel = viewModel()

        viewModel.create(NewBoard(name = "   ", color = "#FF9800"))

        assertTrue(viewModel.state.value.error is AppError.Validation)
        coVerify(exactly = 0) { repository.createBoard(any()) }
    }

    @Test
    fun `delete removes the board from the list`() {
        coEvery { repository.getBoards() } returns AppResult.Success(listOf(board("1"), board("2")))
        coEvery { repository.deleteBoard("1") } returns AppResult.Success(Unit)
        val viewModel = viewModel()

        viewModel.delete("1")

        assertEquals(listOf("2"), viewModel.state.value.boards.map { it.id })
    }

    @Test
    fun `clearError removes the pending error`() {
        coEvery { repository.getBoards() } returns AppResult.Failure(AppError.Unauthorized)
        val viewModel = viewModel()

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }
}