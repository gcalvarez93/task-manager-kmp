// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/ResponseMapping.kt
package com.castrodev.task_manager_kmp.data.remote

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

private const val MAX_ERROR_MESSAGE_LENGTH = 200

/** Traduce una respuesta con error HTTP a un [AppError]. */
internal suspend fun HttpResponse.toAppError(): AppError {
    val text = try {
        bodyAsText()
    } catch (e: Exception) {
        ""
    }
    val message = text.take(MAX_ERROR_MESSAGE_LENGTH).ifBlank { status.description }

    return when (status.value) {
        401, 403 -> AppError.Unauthorized
        404 -> AppError.NotFound
        400, 422 -> AppError.Validation(message)
        else -> AppError.Server(status.value, message)
    }
}

/** Lee el cuerpo como [T] si la respuesta es correcta, o devuelve el error HTTP. */
internal suspend inline fun <reified T> HttpResponse.toResult(): AppResult<T> =
    if (status.isSuccess()) {
        AppResult.Success(body<T>())
    } else {
        AppResult.Failure(toAppError())
    }

/** Para respuestas sin cuerpo (por ejemplo, `204 No Content` al borrar). */
internal suspend fun HttpResponse.toUnitResult(): AppResult<Unit> =
    if (status.isSuccess()) {
        AppResult.Success(Unit)
    } else {
        AppResult.Failure(toAppError())
    }