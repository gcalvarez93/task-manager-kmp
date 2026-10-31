// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/ScreenScope.kt
package com.castrodev.task_manager_kmp.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Scope de coroutinas atado a la vida de una pantalla, para plataformas que no tienen
 * un equivalente a `viewModelScope`. En iOS, cada ViewModel de Swift crea uno, se lo pasa
 * a su store y llama a [close] cuando desaparece, de modo que el trabajo pendiente se cancela.
 *
 * Usa un [SupervisorJob]: el fallo de una operacion no cancela las demas.
 */
class ScreenScope : CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = job + Dispatchers.Main

    /** Cancela todo el trabajo en curso de esta pantalla. */
    fun close() {
        job.cancel()
    }
}