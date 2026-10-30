// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/TokenProvider.kt
package com.castrodev.task_manager_kmp.data.remote

/**
 * Proporciona el token de identidad del usuario (el ID token de Firebase Auth).
 *
 * Cada plataforma lo implementa con su propio SDK de Firebase: asi el modulo compartido
 * no depende de Firebase. Debe devolver `null` si no hay ningun usuario con sesion iniciada.
 */
interface TokenProvider {
    suspend fun idToken(): String?
}