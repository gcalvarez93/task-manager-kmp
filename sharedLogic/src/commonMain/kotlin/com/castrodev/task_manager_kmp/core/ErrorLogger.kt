// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/core/ErrorLogger.kt
package com.castrodev.task_manager_kmp.core

/**
 * Registro de errores del modulo compartido. Cada plataforma lo implementa con su propio sistema
 * de logs (Timber en Android, swift-log en iOS), de modo que el modulo no depende de ninguno.
 *
 * El detalle tecnico de un fallo va aqui y nunca a la pantalla.
 */
interface ErrorLogger {
    fun warn(message: String)
}

/** Logger que no hace nada: para los tests y para quien no necesite registrar. */
object NoOpErrorLogger : ErrorLogger {
    override fun warn(message: String) = Unit
}