// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/login/LoginValidation.kt
package com.castrodev.task_manager_kmp.presentation.login

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

internal fun validatePassword(password: String): PasswordError? =
    if (password.isEmpty()) PasswordError.EMPTY else null