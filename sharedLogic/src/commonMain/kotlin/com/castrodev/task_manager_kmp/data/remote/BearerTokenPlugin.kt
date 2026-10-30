// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/BearerTokenPlugin.kt
package com.castrodev.task_manager_kmp.data.remote

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.bearerAuth

/**
 * Plugin de Ktor que anade `Authorization: Bearer <token>` a cada peticion.
 * El token se pide al [TokenProvider] en cada peticion, para que el SDK de Firebase
 * pueda devolver siempre uno vigente. Si no hay token, la peticion se envia sin cabecera.
 */
internal fun bearerTokenPlugin(tokenProvider: TokenProvider) =
    createClientPlugin("BearerToken") {
        onRequest { request, _ ->
            val token = tokenProvider.idToken()
            if (token != null) {
                request.bearerAuth(token)
            }
        }
    }