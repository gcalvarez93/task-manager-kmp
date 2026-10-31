// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/login/LoginState.kt
package com.castrodev.task_manager_kmp.presentation.login

import com.castrodev.task_manager_kmp.core.AppError

enum class EmailError { EMPTY, INVALID }

enum class PasswordError { EMPTY }

/**
 * Estado de la pantalla de login. Es inmutable: las vistas solo lo leen.
 *
 * @property emailError error de validacion del email, o `null` si es correcto o aun no se ha validado.
 * @property passwordError error de validacion de la contrasena.
 * @property error error devuelto al iniciar sesion (credenciales incorrectas, sin red...).
 * @property isSignedIn `true` cuando hay sesion iniciada; la navegacion reacciona a este valor.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val error: AppError? = null,
    val isSignedIn: Boolean = false,
)