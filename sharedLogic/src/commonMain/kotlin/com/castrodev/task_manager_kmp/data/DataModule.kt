// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/DataModule.kt
package com.castrodev.task_manager_kmp.data

import com.castrodev.task_manager_kmp.data.remote.TokenProvider
import com.castrodev.task_manager_kmp.data.remote.createApiClient
import com.castrodev.task_manager_kmp.data.repository.KtorBoardRepository
import com.castrodev.task_manager_kmp.data.repository.KtorTaskRepository
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository
import io.ktor.client.HttpClient

/**
 * Punto de entrada de la capa de datos para las apps de Android e iOS.
 * Oculta Ktor: las apps solo ven los repositorios del dominio y el [TokenProvider].
 *
 * @param baseUrl URL base de la API, por ejemplo `https://api.castrodev.com`.
 * @param tokenProvider origen del ID token de Firebase del usuario.
 */
class DataModule internal constructor(client: HttpClient) {

    constructor(baseUrl: String, tokenProvider: TokenProvider) : this(createApiClient(baseUrl, tokenProvider))

    val tasks: TaskRepository = KtorTaskRepository(client)
    val boards: BoardRepository = KtorBoardRepository(client)
}