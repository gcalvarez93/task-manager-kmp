// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/domain/usecase/CreateTaskUseCaseTest.kt
package com.castrodev.task_manager_kmp.domain.usecase

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.testing.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CreateTaskUseCaseTest {

    private val repository = FakeTaskRepository()
    private val createTask = CreateTaskUseCase(repository)

    @Test
    fun `blank title is rejected without calling the repository`() = runTest {
        val result = createTask(NewTask(boardId = "board-1", title = "   "))

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `title longer than the maximum is rejected`() = runTest {
        val title = "a".repeat(CreateTaskUseCase.MAX_TITLE_LENGTH + 1)

        val result = createTask(NewTask(boardId = "board-1", title = title))

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `title with exactly the maximum length is accepted`() = runTest {
        val title = "a".repeat(CreateTaskUseCase.MAX_TITLE_LENGTH)

        val result = createTask(NewTask(boardId = "board-1", title = title))

        assertIs<AppResult.Success<*>>(result)
    }

    @Test
    fun `title is trimmed before being sent to the repository`() = runTest {
        createTask(NewTask(boardId = "board-1", title = "  Comprar pan  "))

        assertEquals("Comprar pan", repository.created.single().title)
    }

    @Test
    fun `repository error is returned unchanged`() = runTest {
        repository.failWith = AppError.Unauthorized

        val result = createTask(NewTask(boardId = "board-1", title = "Comprar pan"))

        assertEquals(AppResult.Failure(AppError.Unauthorized), result)
    }
}