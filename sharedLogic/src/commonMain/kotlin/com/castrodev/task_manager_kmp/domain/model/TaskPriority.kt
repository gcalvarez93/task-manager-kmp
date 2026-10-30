// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/model/TaskPriority.kt
package com.castrodev.task_manager_kmp.domain.model

/**
 * Prioridad de una tarea. [apiValue] es el texto que usa la API (`low | medium | high`)
 * y [weight] sirve para ordenar: a mayor peso, mas prioritaria.
 */
enum class TaskPriority(val apiValue: String, val weight: Int) {
    LOW("low", 1),
    MEDIUM("medium", 2),
    HIGH("high", 3);

    companion object {
        fun fromApiValue(value: String): TaskPriority? = entries.firstOrNull { it.apiValue == value }
    }
}