// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/AppContainer.kt
package com.castrodev.task_manager_kmp

import com.castrodev.task_manager_kmp.data.DataModule
import com.castrodev.task_manager_kmp.data.remote.TokenProvider
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository
import com.castrodev.task_manager_kmp.domain.usecase.CreateTaskUseCase
import com.castrodev.task_manager_kmp.domain.usecase.FilterTasksUseCase
import com.castrodev.task_manager_kmp.presentation.tasks.TaskListStore
import kotlinx.coroutines.CoroutineScope

/**
 * Raiz de composicion del modulo compartido: monta las dependencias una sola vez.
 * Cada app (Android e iOS) crea un unico [AppContainer] al arrancar y le pide lo que necesita,
 * sin conocer Ktor ni la estructura interna.
 *
 * @param baseUrl URL base de la API, por ejemplo `https://api.castrodev.com`.
 * @param tokenProvider origen del ID token de Firebase del usuario (lo implementa cada plataforma).
 */
class AppContainer(baseUrl: String, tokenProvider: TokenProvider) {

    private val data = DataModule(baseUrl, tokenProvider)

    val tasks: TaskRepository = data.tasks
    val boards: BoardRepository = data.boards

    private val filterTasks = FilterTasksUseCase()

    /**
     * Crea el store de la lista de tareas.
     *
     * @param boardId tablero a mostrar, o `null` para todas las tareas.
     * @param scope scope de la pantalla; cuando se cancela, el store deja de trabajar.
     */
    fun createTaskListStore(boardId: String?, scope: CoroutineScope): TaskListStore =
        TaskListStore(
            boardId = boardId,
            repository = tasks,
            createTaskUseCase = CreateTaskUseCase(tasks),
            filterTasks = filterTasks,
            scope = scope,
        )
}