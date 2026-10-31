// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/presentation/login/LoginValidationTest.kt
package com.castrodev.task_manager_kmp.presentation.login

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoginValidationTest {

    @Test
    fun `well formed emails are accepted`() {
        listOf("ana@example.com", "a.b+c@sub.example.es", "  ana@example.com  ").forEach { email ->
            assertNull(validateEmail(email), email)
        }
    }

    @Test
    fun `blank email is reported as empty`() {
        assertEquals(EmailError.EMPTY, validateEmail(""))
        assertEquals(EmailError.EMPTY, validateEmail("   "))
    }

    @Test
    fun `malformed emails are reported as invalid`() {
        listOf("ana", "ana@", "@example.com", "ana@example", "ana@@example.com", "ana @example.com", "ana@example.", "ana@.com")
            .forEach { email ->
                assertEquals(EmailError.INVALID, validateEmail(email), email)
            }
    }

    @Test
    fun `empty password is reported and any other is accepted`() {
        assertEquals(PasswordError.EMPTY, validatePassword(""))
        assertNull(validatePassword("a"))
    }
}