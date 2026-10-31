// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/testing/FakeAuthRepository.kt
package com.castrodev.task_manager_kmp.testing

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred

/**
 * Autenticacion en memoria para los tests.
 * - [result]: lo que devuelve [signIn]; si es un exito, la sesion pasa a iniciada.
 * - [gate]: si no es `null`, [signIn] espera a que se complete (para observar el estado de carga).
 */
internal class FakeAuthRepository(private var signedIn: Boolean = false) : AuthRepository {
    var result: AppResult<Unit> = AppResult.Success(Unit)
    var gate: CompletableDeferred<Unit>? = null
    val signInCalls = mutableListOf<Pair<String, String>>()
    var signOutCalls = 0
        private set

    override val isSignedIn: Boolean
        get() = signedIn

    override suspend fun signIn(email: String, password: String): AppResult<Unit> {
        signInCalls += email to password
        gate?.await()
        if (result is AppResult.Success) signedIn = true
        return result
    }

    override fun signOut() {
        signOutCalls++
        signedIn = false
    }
}