// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/repository/AuthRepository.kt
package com.castrodev.task_manager_kmp.domain.repository

import com.castrodev.task_manager_kmp.core.AppResult

/**
 * Autenticacion del usuario. Cada plataforma la implementa con su SDK de Firebase Auth
 * (Android con Kotlin, iOS con Swift), de modo que el modulo compartido no depende de Firebase.
 */
interface AuthRepository {

    /** `true` si ya hay un usuario con sesion iniciada (por ejemplo, de un arranque anterior). */
    val isSignedIn: Boolean

    /** Inicia sesion con email y contrasena. Las credenciales incorrectas devuelven `AppError.Unauthorized`. */
    suspend fun signIn(email: String, password: String): AppResult<Unit>

    fun signOut()
}