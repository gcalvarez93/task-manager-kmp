// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/ApiClientFactory.kt
package com.castrodev.task_manager_kmp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

/** Configuracion comun del cliente: JSON, URL base y token. Se reutiliza en los tests con un motor falso. */
internal fun HttpClientConfig<*>.configureApi(baseUrl: String, tokenProvider: TokenProvider) {
    install(ContentNegotiation) {
        json(apiJson)
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