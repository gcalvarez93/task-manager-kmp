// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/core/AppError.kt
package com.castrodev.task_manager_kmp.core

/**
 * Errores de negocio e infraestructura que la capa de dominio puede devolver.
 * Se devuelven dentro de [AppResult.Failure] en lugar de lanzar excepciones.
 */
sealed interface AppError {
    data object Unauthorized : AppError
    data object NotFound : AppError
    data class Validation(val message: String) : AppError
    data class Network(val message: String) : AppError
    data class Server(val statusCode: Int, val message: String) : AppError
    data class Unknown(val message: String) : AppError
}