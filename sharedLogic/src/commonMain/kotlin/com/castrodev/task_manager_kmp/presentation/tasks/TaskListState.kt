// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/tasks/TaskListState.kt
package com.castrodev.task_manager_kmp.presentation.tasks

import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskStatus

/**
 * Estado completo de la pantalla de tareas. Es inmutable: las vistas solo lo leen.
 *
 * @property allTasks todas las tareas recibidas de la API.
 * @property visibleTasks las tareas que se muestran: filtradas por [filter] y ordenadas.
 * @property filter estado por el que se filtra, o `null` para mostrar todas.
 * @property error ultimo error pendiente de mostrar como aviso (cambiar estado, borrar, o recargar con datos ya en pantalla).
 * @property loadError la carga ha fallado y no hay nada que mostrar: la pantalla debe ofrecer reintentar
 * en lugar de parecer una lista vacia. Se limpia al empezar una nueva carga.
 * @property isCreateFormOpen el formulario de crear tarea esta abierto. Solo se cierra al crear
 * correctamente o cuando el usuario lo cancela: si falla, sigue abierto y conserva lo escrito.
 * @property isCreating hay un intento de crear en curso.
 * @property createError error del ultimo intento de crear; se muestra dentro del formulario.
 */
data class TaskListState(
    val isLoading: Boolean = false,
    val allTasks: List<TaskItem> = emptyList(),
    val visibleTasks: List<TaskItem> = emptyList(),
    val filter: TaskStatus? = null,
    val error: AppError? = null,
    val loadError: AppError? = null,
    val isCreateFormOpen: Boolean = false,
    val isCreating: Boolean = false,
    val createError: AppError? = null,
)