// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/presentation/ScreenScopeTest.kt
package com.castrodev.task_manager_kmp.presentation

import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ScreenScopeTest {

    @Test
    fun `a new scope is active`() {
        val job = assertNotNull(ScreenScope().coroutineContext[Job])

        assertTrue(job.isActive)
    }

    @Test
    fun `close cancels the scope`() {
        val scope = ScreenScope()
        val job = assertNotNull(scope.coroutineContext[Job])

        scope.close()

        assertTrue(job.isCancelled)
    }
}