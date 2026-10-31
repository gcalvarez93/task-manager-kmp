// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/AppContainerTest.kt
package com.castrodev.task_manager_kmp

import com.castrodev.task_manager_kmp.data.remote.FakeTokenProvider
import com.castrodev.task_manager_kmp.presentation.login.LoginState
import com.castrodev.task_manager_kmp.presentation.tasks.TaskListState
import com.castrodev.task_manager_kmp.testing.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppContainerTest {

    private fun container(auth: FakeAuthRepository = FakeAuthRepository()) =
        AppContainer("https://api.test", FakeTokenProvider(), auth)

    /** Comprueba tambien que el motor HTTP de cada plataforma (OkHttp / Darwin) se puede crear. */
    @Test
    fun `container creates a task list store with an empty initial state`() = runTest {
        val store = container().createTaskListStore(boardId = null, scope = backgroundScope)

        assertEquals(TaskListState(), store.state.value)
    }

    @Test
    fun `container creates a login store that reflects the current session`() = runTest {
        val store = container(FakeAuthRepository(signedIn = true)).createLoginStore(backgroundScope)

        assertEquals(LoginState(isSignedIn = true), store.state.value)
        assertTrue(store.state.value.isSignedIn)
    }
}