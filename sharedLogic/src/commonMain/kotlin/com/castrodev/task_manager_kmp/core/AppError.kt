// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/core/AppError.kt
package com.castrodev.task_manager_kmp.core

/**
 * Errores de negocio e infraestructura que la capa de dominio puede devolver.
 * Se devuelven dentro de [AppResult.Failure] en lugar de lanzar excepciones.
 */
sealed interface AppError {
    data object Unauthorized : AppError
    data object NotFound : AppError

    /**
     * Regla de negocio incumplida, comprobada por la propia app antes de llamar a la API.
     * El [message] ya viene redactado para el usuario y se puede mostrar tal cual.
     */
    data class Validation(val message: String) : AppError

    /** No ha sido posible hablar con el servidor (sin conexion, DNS, conexion cortada...). */
    data class Network(val message: String) : AppError

    /** La peticion ha tardado demasiado: el servidor no ha respondido en el tiempo permitido. */
    data object Timeout : AppError

    data class Server(val statusCode: Int, val message: String) : AppError

    /**
     * El servidor ha rechazado los datos enviados (HTTP 400 o 422).
     * [detail] es el texto que devolvio el servidor: sirve para los logs y nunca debe mostrarse al usuario.
     */
    data class Rejected(val statusCode: Int, val detail: String) : AppError

    /** La respuesta del servidor llego, pero su contenido no es valido (estado desconocido, fecha mal formada...). */
    data class InvalidData(val message: String) : AppError

    /** El registro ha fallado porque ya existe una cuenta con ese email. */
    data object EmailAlreadyInUse : AppError

    /** El registro ha fallado porque el proveedor de autenticacion considera debil la contrasena. */
    data object WeakPassword : AppError

    data class Unknown(val message: String) : AppError
}