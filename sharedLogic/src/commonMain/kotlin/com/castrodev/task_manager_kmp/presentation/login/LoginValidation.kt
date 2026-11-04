// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/login/LoginValidation.kt
package com.castrodev.task_manager_kmp.presentation.login

/** Longitud minima de contrasena que exige Firebase Auth al crear una cuenta. */
const val MIN_PASSWORD_LENGTH: Int = 6

/**
 * Validacion basica de formato, solo para avisar al usuario antes de llamar a la red.
 * La comprobacion definitiva la hace Firebase.
 */
internal fun validateEmail(email: String): EmailError? {
    val value = email.trim()
    if (value.isEmpty()) return EmailError.EMPTY

    val at = value.indexOf('@')
    val lastDot = value.lastIndexOf('.')
    val isValid = at > 0 &&
            at == value.lastIndexOf('@') &&
            lastDot > at + 1 &&
            lastDot < value.length - 1 &&
            value.none { it.isWhitespace() }

    return if (isValid) null else EmailError.INVALID
}

/** Al iniciar sesion solo se exige que no este vacia; al registrarse, ademas, [MIN_PASSWORD_LENGTH] caracteres. */
internal fun validatePassword(password: String, mode: AuthMode = AuthMode.SIGN_IN): PasswordError? = when {
    password.isEmpty() -> PasswordError.EMPTY
    mode == AuthMode.SIGN_UP && password.length < MIN_PASSWORD_LENGTH -> PasswordError.TOO_SHORT
    else -> null
}