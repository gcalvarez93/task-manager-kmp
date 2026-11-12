// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/data/repository/LoggingRepositoriesTest.kt
package com.castrodev.task_manager_kmp.data.repository

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.core.ErrorLogger
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.testing.FakeBoardRepository
import com.castrodev.task_manager_kmp.testing.FakeTaskRepository
import com.castrodev.task_manager_kmp.testing.boardItem
import com.castrodev.task_manager_kmp.testing.taskItem
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoggingRepositoriesTest {

    private class RecordingLogger : ErrorLogger {
        val messages = mutableListOf<String>()
        override fun warn(message: String) {
            messages += message
        }
    }

    @Test
    fun `a failing task operation is logged with its detail and the result is unchanged`() = runTest {
        val logger = RecordingLogger()
        val fake = FakeTaskRepository().apply { failWith = AppError.Rejected(400, "titulo vacio") }
        val repository = LoggingTaskRepository(fake, logger)

        val result = repository.createTask(NewTask(boardId = "board-1", title = "x"))

        assertEquals(AppResult.Failure(AppError.Rejected(400, "titulo vacio")), result)
        val message = logger.messages.single()
        assertTrue(message.contains("tareas.createTask"), message)
        assertTrue(message.contains("titulo vacio"), message)
    }

    @Test
    fun `a successful task operation is not logged`() = runTest {
        val logger = RecordingLogger()
        val repository = LoggingTaskRepository(FakeTaskRepository(listOf(taskItem("1"))), logger)

        val result = repository.getTasks(null)

        assertTrue(result is AppResult.Success)
        assertTrue(logger.messages.isEmpty())
    }

    @Test
    fun `a failing board operation is logged and the result is unchanged`() = runTest {
        val logger = RecordingLogger()
        val fake = FakeBoardRepository().apply { failWith = AppError.Timeout }
        val repository = LoggingBoardRepository(fake, logger)

        val result = repository.getBoards()

        assertEquals(AppResult.Failure(AppError.Timeout), result)
        assertTrue(logger.messages.single().contains("tableros.getBoards"))
    }

    @Test
    fun `a successful board operation is not logged`() = runTest {
        val logger = RecordingLogger()
        val repository = LoggingBoardRepository(FakeBoardRepository(listOf(boardItem("1"))), logger)

        repository.createBoard(NewBoard(name = "Casa", color = "#FF9800"))

        assertTrue(logger.messages.isEmpty())
    }
}