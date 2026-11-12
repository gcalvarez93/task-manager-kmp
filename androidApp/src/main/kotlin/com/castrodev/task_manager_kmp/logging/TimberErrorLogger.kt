// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/logging/TimberErrorLogger.kt
package com.castrodev.task_manager_kmp.logging

import com.castrodev.task_manager_kmp.core.ErrorLogger
import timber.log.Timber

/** Lleva los errores del modulo compartido a Timber. Si no hay ningun `Tree` plantado, no se escribe nada. */
class TimberErrorLogger : ErrorLogger {
    override fun warn(message: String) {
        Timber.w(message)
    }
}