// Path: sharedLogic/src/commonTest/kotlin/com/castrodev/task_manager_kmp/data/remote/TestSupport.kt
package com.castrodev.task_manager_kmp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/** Proveedor de token falso para los tests. Con `token = null` simula que no hay sesion. */
internal class FakeTokenProvider(private val token: String? = "token-123") : TokenProvider {
    override suspend fun idToken(): String? = token
}

/** Crea un cliente con la misma configuracion que el real, pero con un motor falso. */
internal fun testClient(
    engine: MockEngine,
    tokenProvider: TokenProvider = FakeTokenProvider(),
): HttpClient = HttpClient(engine) {
    configureApi("https://api.test", tokenProvider)
}

/** Respuesta JSON del motor falso. */
internal fun MockRequestHandleScope.jsonResponse(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = body,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)