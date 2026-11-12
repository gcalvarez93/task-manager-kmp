// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/DataModule.kt
package com.castrodev.task_manager_kmp.data

import com.castrodev.task_manager_kmp.core.ErrorLogger
import com.castrodev.task_manager_kmp.core.NoOpErrorLogger
import com.castrodev.task_manager_kmp.data.remote.TokenProvider
import com.castrodev.task_manager_kmp.data.remote.createApiClient
import com.castrodev.task_manager_kmp.data.repository.KtorBoardRepository
import com.castrodev.task_manager_kmp.data.repository.KtorTaskRepository
import com.castrodev.task_manager_kmp.data.repository.LoggingBoardRepository
import com.castrodev.task_manager_kmp.data.repository.LoggingTaskRepository
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository
import io.ktor.client.HttpClient

/**
 * Punto de entrada de la capa de datos para las apps de Android e iOS.
 * Oculta Ktor: las apps solo ven los repositorios del dominio y el [TokenProvider].
 * Cada operacion que falla se registra en el [ErrorLogger].
 *
 * @param baseUrl URL base de la API, por ejemplo `https://api.castrodev.com`.
 * @param tokenProvider origen del ID token de Firebase del usuario.
 * @param logger destino de los logs de errores (lo implementa cada plataforma).
 */
class DataModule internal constructor(client: HttpClient, logger: ErrorLogger = NoOpErrorLogger) {

    constructor(baseUrl: String, tokenProvider: TokenProvider) : this(baseUrl, tokenProvider, NoOpErrorLogger)

    constructor(baseUrl: String, tokenProvider: TokenProvider, logger: ErrorLogger) :
            this(createApiClient(baseUrl, tokenProvider), logger)

    val tasks: TaskRepository = LoggingTaskRepository(KtorTaskRepository(client), logger)
    val boards: BoardRepository = LoggingBoardRepository(KtorBoardRepository(client), logger)
}