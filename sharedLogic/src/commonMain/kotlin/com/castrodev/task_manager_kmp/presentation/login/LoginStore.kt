// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/login/LoginStore.kt
// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/login/LoginStore.kt
package com.castrodev.task_manager_kmp.presentation.login

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Logica de presentacion compartida del login y el registro. Android e iOS solo pintan [state]
 * y le pasan lo que escribe el usuario; la validacion y el flujo viven aqui, una sola vez.
 */
class LoginStore(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(LoginState(isSignedIn = authRepository.isSignedIn))
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, error = null) }
    }

    /**
     * Alterna entre iniciar sesion y crear cuenta. Conserva lo escrito, limpia los errores
     * y se ignora mientras hay un intento en curso.
     */
    fun toggleMode() {
        _state.update {
            if (it.isLoading) {
                it
            } else {
                it.copy(
                    mode = if (it.mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN,
                    emailError = null,
                    passwordError = null,
                    error = null,
                )
            }
        }
    }

    /**
     * Valida los campos y, si son correctos, inicia sesion o crea la cuenta segun [LoginState.mode].
     * Se ignora si ya hay un intento en curso.
     */
    fun submit() {
        val current = _state.value
        if (current.isLoading) return

        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password, current.mode)
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _state.update { it.copy(isLoading = true, emailError = null, passwordError = null, error = null) }
        scope.launch {
            val email = current.email.trim()
            val result = when (current.mode) {
                AuthMode.SIGN_IN -> authRepository.signIn(email, current.password)
                AuthMode.SIGN_UP -> authRepository.signUp(email, current.password)
            }
            when (result) {
                is AppResult.Success -> _state.update {
                    it.copy(isLoading = false, isSignedIn = true, password = "")
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _state.value = LoginState()
    }

    /** La vista llama a esto despues de mostrar el error. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}