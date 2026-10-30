// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/model/TaskStatus.kt
package com.castrodev.task_manager_kmp.domain.model

/** Estado de una tarea. [apiValue] es el texto que usa la API (`todo | inProgress | done`). */
enum class TaskStatus(val apiValue: String) {
    PENDING("todo"),
    IN_PROGRESS("inProgress"),
    DONE("done");

    companion object {
        fun fromApiValue(value: String): TaskStatus? = entries.firstOrNull { it.apiValue == value }
    }
}