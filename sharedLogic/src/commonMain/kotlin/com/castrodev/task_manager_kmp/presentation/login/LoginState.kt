// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/login/LoginState.kt
package com.castrodev.task_manager_kmp.presentation.login

import com.castrodev.task_manager_kmp.core.AppError

enum class EmailError { EMPTY, INVALID }

/** [TOO_SHORT] solo se produce al registrarse: iniciar sesion no impone longitud minima. */
enum class PasswordError { EMPTY, TOO_SHORT }

/** Accion que ejecuta el boton principal de la pantalla. */
enum class AuthMode { SIGN_IN, SIGN_UP }

/**
 * Estado de la pantalla de login y registro. Es inmutable: las vistas solo lo leen.
 *
 * @property mode si el boton principal inicia sesion o crea una cuenta.
 * @property emailError error de validacion del email, o `null` si es correcto o aun no se ha validado.
 * @property passwordError error de validacion de la contrasena.
 * @property error error devuelto por la autenticacion (credenciales incorrectas, email en uso, sin red...).
 * @property isSignedIn `true` cuando hay sesion iniciada; la navegacion reacciona a este valor.
 */
data class LoginState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val error: AppError? = null,
    val isSignedIn: Boolean = false,
)