// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/remote/ApiJson.kt
package com.castrodev.task_manager_kmp.data.remote

import kotlinx.serialization.json.Json

/**
 * Configuracion JSON compartida por el cliente HTTP y los tests.
 *
 * - `ignoreUnknownKeys`: la API puede anadir campos sin romper la app.
 * - `encodeDefaults`: enviamos siempre los valores por defecto (por ejemplo `labels: []`),
 *   porque la API espera la lista y la recibiria como `null` si faltara.
 */
internal val apiJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}