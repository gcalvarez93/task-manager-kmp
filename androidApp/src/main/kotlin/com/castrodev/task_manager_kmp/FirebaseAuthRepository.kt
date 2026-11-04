// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/FirebaseAuthRepository.kt
package com.castrodev.task_manager_kmp

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/** Implementacion de [AuthRepository] con Firebase Auth (email y contrasena). */
class FirebaseAuthRepository(private val auth: FirebaseAuth) : AuthRepository {

    override val isSignedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun signIn(email: String, password: String): AppResult<Unit> =
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AppResult.Failure(AppError.Unauthorized)
        } catch (e: FirebaseAuthInvalidUserException) {
            AppResult.Failure(AppError.Unauthorized)
        } catch (e: FirebaseNetworkException) {
            AppResult.Failure(AppError.Network(e.message ?: "Sin conexion"))
        } catch (e: FirebaseTooManyRequestsException) {
            AppResult.Failure(AppError.Unknown("Demasiados intentos. Prueba de nuevo mas tarde"))
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e.message ?: "Error al iniciar sesion"))
        }

    /** Crea la cuenta; Firebase deja la sesion iniciada con el usuario recien creado. */
    override suspend fun signUp(email: String, password: String): AppResult<Unit> =
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthUserCollisionException) {
            AppResult.Failure(AppError.EmailAlreadyInUse)
        } catch (e: FirebaseAuthWeakPasswordException) {
            // Subclase de FirebaseAuthInvalidCredentialsException: debe capturarse antes.
            AppResult.Failure(AppError.WeakPassword)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            // En el registro, las credenciales invalidas son un email mal formado.
            AppResult.Failure(AppError.Validation(e.message ?: "Email no valido"))
        } catch (e: FirebaseNetworkException) {
            AppResult.Failure(AppError.Network(e.message ?: "Sin conexion"))
        } catch (e: FirebaseTooManyRequestsException) {
            AppResult.Failure(AppError.Unknown("Demasiados intentos. Prueba de nuevo mas tarde"))
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e.message ?: "Error al crear la cuenta"))
        }

    override fun signOut() {
        auth.signOut()
    }
}