// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/FirebaseTokenProvider.kt
package com.castrodev.task_manager_kmp

import com.castrodev.task_manager_kmp.data.remote.TokenProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Entrega al modulo compartido el ID token del usuario con sesion iniciada en Firebase Auth.
 *
 * `getIdToken(false)` devuelve el token en cache y solo lo renueva cuando esta a punto
 * de caducar, asi que se puede llamar en cada peticion sin coste adicional.
 */
class FirebaseTokenProvider(private val auth: FirebaseAuth) : TokenProvider {

    override suspend fun idToken(): String? {
        val user = auth.currentUser ?: return null
        return user.getIdToken(false).await().token
    }
}