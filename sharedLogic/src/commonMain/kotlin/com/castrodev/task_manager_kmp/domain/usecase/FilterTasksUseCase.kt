// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/usecase/FilterTasksUseCase.kt
package com.castrodev.task_manager_kmp.domain.usecase

import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import kotlin.time.Instant

/**
 * Filtra las tareas por estado (si se indica) y las ordena por prioridad,
 * de mayor a menor, y despues por fecha limite, con las que no tienen fecha al final.
 */
class FilterTasksUseCase {

    operator fun invoke(tasks: List<TaskItem>, status: TaskStatus? = null): List<TaskItem> =
        tasks
            .filter { status == null || it.status == status }
            .sortedWith(
                compareByDescending<TaskItem> { it.priority.weight }
                    .thenBy(nullsLast<Instant>()) { it.dueDate },
            )
}