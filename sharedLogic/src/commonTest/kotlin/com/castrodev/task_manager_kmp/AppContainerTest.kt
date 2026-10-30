// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/AppContainerTest.kt
package com.castrodev.task_manager_kmp

import com.castrodev.task_manager_kmp.data.remote.FakeTokenProvider
import com.castrodev.task_manager_kmp.presentation.tasks.TaskListState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppContainerTest {

    /** Comprueba tambien que el motor HTTP de cada plataforma (OkHttp / Darwin) se puede crear. */
    @Test
    fun `container creates a task list store with an empty initial state`() = runTest {
        val container = AppContainer("https://api.test", FakeTokenProvider())

        val store = container.createTaskListStore(boardId = null, scope = backgroundScope)

        assertEquals(TaskListState(), store.state.value)
    }
}