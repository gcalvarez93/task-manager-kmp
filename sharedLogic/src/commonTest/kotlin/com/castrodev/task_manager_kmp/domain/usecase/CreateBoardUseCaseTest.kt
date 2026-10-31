// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/domain/usecase/CreateBoardUseCaseTest.kt
package com.castrodev.task_manager_kmp.domain.usecase

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.testing.FakeBoardRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CreateBoardUseCaseTest {

    private val repository = FakeBoardRepository()
    private val createBoard = CreateBoardUseCase(repository)

    @Test
    fun `blank name is rejected without calling the repository`() = runTest {
        val result = createBoard(NewBoard(name = "   ", color = "#FF9800"))

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `name longer than the maximum is rejected`() = runTest {
        val name = "a".repeat(CreateBoardUseCase.MAX_NAME_LENGTH + 1)

        val result = createBoard(NewBoard(name = name, color = "#FF9800"))

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `name is trimmed before being sent to the repository`() = runTest {
        createBoard(NewBoard(name = "  Casa  ", color = "#FF9800"))

        assertEquals("Casa", repository.created.single().name)
    }

    @Test
    fun `repository error is returned unchanged`() = runTest {
        repository.failWith = AppError.Unauthorized

        val result = createBoard(NewBoard(name = "Casa", color = "#FF9800"))

        assertEquals(AppResult.Failure(AppError.Unauthorized), result)
    }
}