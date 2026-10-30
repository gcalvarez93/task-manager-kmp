// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/core/AppResultTest.kt
package com.castrodev.task_manager_kmp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppResultTest {

    @Test
    fun `map transforms the value of a success`() {
        val result: AppResult<Int> = AppResult.Success(2)

        val mapped = result.map { it * 10 }

        assertEquals(AppResult.Success(20), mapped)
    }

    @Test
    fun `map keeps the error of a failure`() {
        val result: AppResult<Int> = AppResult.Failure(AppError.Unauthorized)

        val mapped = result.map { it * 10 }

        assertEquals(AppResult.Failure(AppError.Unauthorized), mapped)
    }

    @Test
    fun `fold calls onSuccess for a success`() {
        val result: AppResult<Int> = AppResult.Success(5)

        val text = result.fold(
            onSuccess = { "ok $it" },
            onFailure = { "error" },
        )

        assertEquals("ok 5", text)
    }

    @Test
    fun `fold calls onFailure for a failure`() {
        val result: AppResult<Int> = AppResult.Failure(AppError.NotFound)

        val text = result.fold(
            onSuccess = { "ok $it" },
            onFailure = { "error $it" },
        )

        assertEquals("error NotFound", text)
    }

    @Test
    fun `getOrNull returns the value for a success and null for a failure`() {
        val success: AppResult<String> = AppResult.Success("hola")
        val failure: AppResult<String> = AppResult.Failure(AppError.Network("sin conexion"))

        assertEquals("hola", success.getOrNull())
        assertNull(failure.getOrNull())
    }
}