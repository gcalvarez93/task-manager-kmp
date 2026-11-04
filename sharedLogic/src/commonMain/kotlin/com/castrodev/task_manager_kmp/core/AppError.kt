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

    /** La respuesta del servidor llego, pero su contenido no es valido (estado desconocido, fecha mal formada...). */
    data class InvalidData(val message: String) : AppError

    /** El registro ha fallado porque ya existe una cuenta con ese email. */
    data object EmailAlreadyInUse : AppError

    /** El registro ha fallado porque el proveedor de autenticacion considera debil la contrasena. */
    data object WeakPassword : AppError

    data class Unknown(val message: String) : AppError
}