// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/usecase/CreateTaskUseCase.kt
package com.castrodev.task_manager_kmp.domain.usecase

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.repository.TaskRepository

/**
 * Crea una tarea aplicando las reglas de negocio antes de llamar a la API:
 * el titulo no puede estar vacio y tiene un maximo de [MAX_TITLE_LENGTH] caracteres.
 * Los espacios al principio y al final del titulo se eliminan.
 */
class CreateTaskUseCase(private val repository: TaskRepository) {

    suspend operator fun invoke(task: NewTask): AppResult<TaskItem> {
        val title = task.title.trim()
        if (title.isEmpty()) {
            return AppResult.Failure(AppError.Validation("El titulo no puede estar vacio"))
        }
        if (title.length > MAX_TITLE_LENGTH) {
            return AppResult.Failure(
                AppError.Validation("El titulo no puede superar los $MAX_TITLE_LENGTH caracteres"),
            )
        }
        return repository.createTask(task.copy(title = title))
    }

    companion object {
        const val MAX_TITLE_LENGTH = 120
    }
}