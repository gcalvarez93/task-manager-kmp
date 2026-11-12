// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/SafeApiCall.kt
package com.castrodev.task_manager_kmp.data.remote

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.serialization.ContentConvertException
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Ejecuta una llamada a la API y convierte cualquier excepcion en un [AppResult.Failure].
 * La cancelacion de la corrutina no se captura: debe propagarse.
 *
 * Los tres tipos de timeout de Ktor (de peticion, de conexion y de lectura/escritura) son un
 * unico [AppError.Timeout]; el resto de fallos de transporte son [AppError.Network].
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
        AppResult.Failure(if (e.isTimeout()) AppError.Timeout else AppError.Network(e.describe()))
    }

private fun Throwable.isTimeout(): Boolean =
    this is HttpRequestTimeoutException ||
            this is ConnectTimeoutException ||
            this is SocketTimeoutException

/** Tipo y mensaje de la excepcion: es texto para los logs, no para el usuario. */
private fun Throwable.describe(): String =
    "${this::class.simpleName ?: "Exception"}: ${message ?: "sin detalle"}"