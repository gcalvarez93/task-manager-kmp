// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/presentation/login/LoginStoreTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.castrodev.task_manager_kmp.presentation.login

import app.cash.turbine.test
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.testing.FakeAuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginStoreTest {

    private fun TestScope.storeWith(auth: FakeAuthRepository) =
        LoginStore(authRepository = auth, scope = backgroundScope)

    private fun LoginStore.fill(email: String = "ana@example.com", password: String = "secreto") {
        onEmailChange(email)
        onPasswordChange(password)
    }

    @Test
    fun `initial state reflects an existing session`() = runTest {
        assertFalse(storeWith(FakeAuthRepository(signedIn = false)).state.value.isSignedIn)
        assertTrue(storeWith(FakeAuthRepository(signedIn = true)).state.value.isSignedIn)
    }

    @Test
    fun `invalid email shows an error and does not call the repository`() = runTest {
        val auth = FakeAuthRepository()
        val store = storeWith(auth)
        store.fill(email = "ana")

        store.submit()
        runCurrent()

        assertEquals(EmailError.INVALID, store.state.value.emailError)
        assertNull(store.state.value.passwordError)
        assertTrue(auth.signInCalls.isEmpty())
    }

    @Test
    fun `empty fields show both errors`() = runTest {
        val auth = FakeAuthRepository()
        val store = storeWith(auth)

        store.submit()
        runCurrent()

        assertEquals(EmailError.EMPTY, store.state.value.emailError)
        assertEquals(PasswordError.EMPTY, store.state.value.passwordError)
        assertTrue(auth.signInCalls.isEmpty())
    }

    @Test
    fun `submit emits loading and then the signed in state`() = runTest {
        val auth = FakeAuthRepository()
        val gate = CompletableDeferred<Unit>()
        auth.gate = gate
        val store = storeWith(auth)

        store.state.test {
            assertFalse(awaitItem().isSignedIn)
            store.onEmailChange("ana@example.com")
            awaitItem()
            store.onPasswordChange("secreto")
            awaitItem()

            store.submit()
            assertTrue(awaitItem().isLoading)

            gate.complete(Unit)
            val done = awaitItem()
            assertFalse(done.isLoading)
            assertTrue(done.isSignedIn)
            assertEquals("", done.password)
        }
    }

    @Test
    fun `failed sign in exposes the error and keeps the form`() = runTest {
        val auth = FakeAuthRepository().apply { result = AppResult.Failure(AppError.Unauthorized) }
        val store = storeWith(auth)
        store.fill()

        store.submit()
        runCurrent()

        val state = store.state.value
        assertEquals(AppError.Unauthorized, state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isSignedIn)
        assertEquals("secreto", state.password)
    }

    @Test
    fun `email is trimmed before signing in`() = runTest {
        val auth = FakeAuthRepository()
        val store = storeWith(auth)
        store.fill(email = "  ana@example.com  ")

        store.submit()
        runCurrent()

        assertEquals("ana@example.com" to "secreto", auth.signInCalls.single())
    }

    @Test
    fun `editing a field clears its error`() = runTest {
        val store = storeWith(FakeAuthRepository())
        store.submit()
        runCurrent()

        store.onEmailChange("a")
        assertNull(store.state.value.emailError)
        assertEquals(PasswordError.EMPTY, store.state.value.passwordError)

        store.onPasswordChange("b")
        assertNull(store.state.value.passwordError)
    }

    @Test
    fun `submit is ignored while a sign in is in progress`() = runTest {
        val auth = FakeAuthRepository()
        val gate = CompletableDeferred<Unit>()
        auth.gate = gate
        val store = storeWith(auth)
        store.fill()

        store.submit()
        runCurrent()
        store.submit()
        runCurrent()
        gate.complete(Unit)
        runCurrent()

        assertEquals(1, auth.signInCalls.size)
    }

    @Test
    fun `sign out clears the session and resets the state`() = runTest {
        val auth = FakeAuthRepository(signedIn = true)
        val store = storeWith(auth)
        store.fill()

        store.signOut()

        assertEquals(1, auth.signOutCalls)
        assertFalse(auth.isSignedIn)
        assertEquals(LoginState(), store.state.value)
    }

    @Test
    fun `clearError removes the pending error`() = runTest {
        val auth = FakeAuthRepository().apply { result = AppResult.Failure(AppError.Network("sin conexion")) }
        val store = storeWith(auth)
        store.fill()
        store.submit()
        runCurrent()
        assertTrue(store.state.value.error is AppError.Network)

        store.clearError()

        assertNull(store.state.value.error)
    }

    // --- Registro ---

    @Test
    fun `toggleMode switches between sign in and sign up`() = runTest {
        val store = storeWith(FakeAuthRepository())
        assertEquals(AuthMode.SIGN_IN, store.state.value.mode)

        store.toggleMode()
        assertEquals(AuthMode.SIGN_UP, store.state.value.mode)

        store.toggleMode()
        assertEquals(AuthMode.SIGN_IN, store.state.value.mode)
    }

    @Test
    fun `toggleMode keeps what was typed and clears the errors`() = runTest {
        val auth = FakeAuthRepository().apply { result = AppResult.Failure(AppError.Unauthorized) }
        val store = storeWith(auth)
        store.fill()
        store.submit()
        runCurrent()
        assertEquals(AppError.Unauthorized, store.state.value.error)

        store.toggleMode()

        val state = store.state.value
        assertEquals(AuthMode.SIGN_UP, state.mode)
        assertEquals("ana@example.com", state.email)
        assertEquals("secreto", state.password)
        assertNull(state.error)
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun `toggleMode is ignored while a request is in progress`() = runTest {
        val auth = FakeAuthRepository()
        val gate = CompletableDeferred<Unit>()
        auth.gate = gate
        val store = storeWith(auth)
        store.fill()
        store.submit()
        runCurrent()
        assertTrue(store.state.value.isLoading)

        store.toggleMode()

        assertEquals(AuthMode.SIGN_IN, store.state.value.mode)
        gate.complete(Unit)
        runCurrent()
    }

    @Test
    fun `sign up creates the account instead of signing in and trims the email`() = runTest {
        val auth = FakeAuthRepository()
        val store = storeWith(auth)
        store.toggleMode()
        store.fill(email = "  nueva@example.com ", password = "secreto1")

        store.submit()
        runCurrent()

        assertEquals("nueva@example.com" to "secreto1", auth.signUpCalls.single())
        assertTrue(auth.signInCalls.isEmpty())
        assertTrue(store.state.value.isSignedIn)
        assertEquals("", store.state.value.password)
    }

    @Test
    fun `sign up rejects a short password without calling the repository`() = runTest {
        val auth = FakeAuthRepository()
        val store = storeWith(auth)
        store.toggleMode()
        store.fill(password = "12345")

        store.submit()
        runCurrent()

        assertEquals(PasswordError.TOO_SHORT, store.state.value.passwordError)
        assertNull(store.state.value.emailError)
        assertTrue(auth.signUpCalls.isEmpty())
    }

    @Test
    fun `a short password is still accepted when signing in`() = runTest {
        val auth = FakeAuthRepository()
        val store = storeWith(auth)
        store.fill(password = "12345")

        store.submit()
        runCurrent()

        assertNull(store.state.value.passwordError)
        assertEquals("ana@example.com" to "12345", auth.signInCalls.single())
    }

    @Test
    fun `sign up with an email already in use exposes the error and keeps the form`() = runTest {
        val auth = FakeAuthRepository().apply { result = AppResult.Failure(AppError.EmailAlreadyInUse) }
        val store = storeWith(auth)
        store.toggleMode()
        store.fill()

        store.submit()
        runCurrent()

        val state = store.state.value
        assertEquals(AppError.EmailAlreadyInUse, state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isSignedIn)
        assertEquals(AuthMode.SIGN_UP, state.mode)
        assertEquals("secreto", state.password)
    }

    @Test
    fun `sign up with a weak password exposes the error`() = runTest {
        val auth = FakeAuthRepository().apply { result = AppResult.Failure(AppError.WeakPassword) }
        val store = storeWith(auth)
        store.toggleMode()
        store.fill()

        store.submit()
        runCurrent()

        assertEquals(AppError.WeakPassword, store.state.value.error)
        assertFalse(store.state.value.isSignedIn)
    }
}