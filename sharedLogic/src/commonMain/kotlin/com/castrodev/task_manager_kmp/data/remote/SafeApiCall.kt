// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/SafeApiCall.kt
package com.castrodev.task_manager_kmp.data.remote

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import io.ktor.serialization.ContentConvertException
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Ejecuta una llamada a la API y convierte cualquier excepcion en un [AppResult.Failure].
 * La cancelacion de la corrutina no se captura: debe propagarse.
 */
internal suspend fun <T> safeApiCall(block: suspend () -> AppResult<T>): AppResult<T> =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: ContentConvertException) {
        AppResult.Failure(AppError.InvalidData(e.message ?: "Respuesta con formato invalido"))
    } catch (e: SerializationException) {
        AppResult.Failure(AppError.InvalidData(e.message ?: "Respuesta con formato invalido"))
    } catch (e: Exception) {
        AppResult.Failure(AppError.Network(e.message ?: "Error de red"))
    }