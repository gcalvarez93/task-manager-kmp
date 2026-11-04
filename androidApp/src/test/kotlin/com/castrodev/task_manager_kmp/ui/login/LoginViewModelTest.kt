// Path: androidApp/src/test/kotlin/com/castrodev/task_manager_kmp/ui/login/LoginViewModelTest.kt
package com.castrodev.task_manager_kmp.ui.login

import app.cash.turbine.test
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.data.remote.TokenProvider
import com.castrodev.task_manager_kmp.domain.repository.AuthRepository
import com.castrodev.task_manager_kmp.presentation.login.AuthMode
import com.castrodev.task_manager_kmp.presentation.login.EmailError
import com.castrodev.task_manager_kmp.presentation.login.PasswordError
import com.castrodev.task_manager_kmp.ui.testing.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class LoginViewModelTest {

    private val auth = mockk<AuthRepository>()

    private fun viewModel(signedIn: Boolean = false): LoginViewModel {
        every { auth.isSignedIn } returns signedIn
        val container = AppContainer("https://api.test/", mockk<TokenProvider>(), auth)
        return LoginViewModel(container)
    }

    @Test
    fun `starts without session and without errors`() {
        val viewModel = viewModel(signedIn = false)

        assertFalse(viewModel.state.value.isSignedIn)
        assertNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun `starts signed in when there is an existing session`() {
        val viewModel = viewModel(signedIn = true)

        assertTrue(viewModel.state.value.isSignedIn)
    }

    @Test
    fun `submitting empty fields shows validation errors and does not call the repository`() {
        val viewModel = viewModel()

        viewModel.submit()

        assertEquals(EmailError.EMPTY, viewModel.state.value.emailError)
        assertEquals(PasswordError.EMPTY, viewModel.state.value.passwordError)
        coVerify(exactly = 0) { auth.signIn(any(), any()) }
    }

    @Test
    fun `a successful sign in updates the state and sends the trimmed email`() {
        coEvery { auth.signIn(any(), any()) } returns AppResult.Success(Unit)
        val viewModel = viewModel()

        viewModel.onEmailChange("  usuario@test.com ")
        viewModel.onPasswordChange("secreto1")
        viewModel.submit()

        assertTrue(viewModel.state.value.isSignedIn)
        coVerify(exactly = 1) { auth.signIn("usuario@test.com", "secreto1") }
    }

    @Test
    fun `wrong credentials expose an unauthorized error`() {
        coEvery { auth.signIn(any(), any()) } returns AppResult.Failure(AppError.Unauthorized)
        val viewModel = viewModel()

        viewModel.onEmailChange("usuario@test.com")
        viewModel.onPasswordChange("incorrecta")
        viewModel.submit()

        assertEquals(AppError.Unauthorized, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSignedIn)
    }

    @Test
    fun `the state shows loading while the sign in is in progress`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { auth.signIn(any(), any()) } coAnswers {
            gate.await()
            AppResult.Success(Unit)
        }
        val viewModel = viewModel()
        viewModel.onEmailChange("usuario@test.com")
        viewModel.onPasswordChange("secreto1")

        viewModel.state.test {
            awaitItem() // estado inicial

            viewModel.submit()
            assertTrue(awaitItem().isLoading)

            gate.complete(Unit)
            val final = expectMostRecentItem()
            assertFalse(final.isLoading)
            assertTrue(final.isSignedIn)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleMode switches the screen to sign up and back`() {
        val viewModel = viewModel()
        assertEquals(AuthMode.SIGN_IN, viewModel.state.value.mode)

        viewModel.toggleMode()
        assertEquals(AuthMode.SIGN_UP, viewModel.state.value.mode)

        viewModel.toggleMode()
        assertEquals(AuthMode.SIGN_IN, viewModel.state.value.mode)
    }

    @Test
    fun `in sign up mode submitting creates the account instead of signing in`() {
        coEvery { auth.signUp(any(), any()) } returns AppResult.Success(Unit)
        val viewModel = viewModel()

        viewModel.toggleMode()
        viewModel.onEmailChange("  nuevo@test.com ")
        viewModel.onPasswordChange("secreto1")
        viewModel.submit()

        assertTrue(viewModel.state.value.isSignedIn)
        coVerify(exactly = 1) { auth.signUp("nuevo@test.com", "secreto1") }
        coVerify(exactly = 0) { auth.signIn(any(), any()) }
    }
}