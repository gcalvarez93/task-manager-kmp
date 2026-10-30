// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/domain/model/TaskStatusTest.kt
package com.castrodev.task_manager_kmp.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TaskStatusTest {

    @Test
    fun `fromApiValue maps every known api value`() {
        assertEquals(TaskStatus.PENDING, TaskStatus.fromApiValue("todo"))
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromApiValue("inProgress"))
        assertEquals(TaskStatus.DONE, TaskStatus.fromApiValue("done"))
    }

    @Test
    fun `fromApiValue returns null for an unknown value`() {
        assertNull(TaskStatus.fromApiValue("archived"))
        assertNull(TaskStatus.fromApiValue(""))
    }

    @Test
    fun `apiValue round trips for every status`() {
        TaskStatus.entries.forEach { status ->
            assertEquals(status, TaskStatus.fromApiValue(status.apiValue))
        }
    }
}