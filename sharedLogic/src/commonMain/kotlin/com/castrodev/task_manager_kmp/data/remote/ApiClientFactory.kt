// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/ApiClientFactory.kt
package com.castrodev.task_manager_kmp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

private const val REQUEST_TIMEOUT_MILLIS = 30_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val SOCKET_TIMEOUT_MILLIS = 30_000L

/**
 * Configuracion comun del cliente: JSON, URL base, timeouts y token.
 * Se reutiliza en los tests con un motor falso.
 *
 * Los timeouts son explicitos para que una red que no responde acabe en un error tipado
 * ([com.castrodev.task_manager_kmp.core.AppError.Timeout]) y no dependa del valor por defecto de cada motor.
 */
internal fun HttpClientConfig<*>.configureApi(baseUrl: String, tokenProvider: TokenProvider) {
    install(ContentNegotiation) {
        json(apiJson)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
    }
    install(DefaultRequest) {
        url(baseUrl)
    }
    install(bearerTokenPlugin(tokenProvider))
}

/**
 * Crea el cliente HTTP de la app. El motor (OkHttp en Android, Darwin en iOS)
 * lo elige Ktor segun la plataforma.
 */
internal fun createApiClient(baseUrl: String, tokenProvider: TokenProvider): HttpClient =
    HttpClient {
        configureApi(baseUrl, tokenProvider)
    }