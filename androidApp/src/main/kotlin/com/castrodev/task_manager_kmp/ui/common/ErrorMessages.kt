// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/common/ErrorMessages.kt
package com.castrodev.task_manager_kmp.ui.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.castrodev.task_manager_kmp.R
import com.castrodev.task_manager_kmp.core.AppError

/** Texto para el usuario de un [AppError]. Los mensajes de validacion ya vienen redactados. */
@Composable
fun AppError.asMessage(): String = when (this) {
    is AppError.Validation -> message
    is AppError.Unauthorized -> stringResource(R.string.error_unauthorized)
    is AppError.NotFound -> stringResource(R.string.error_not_found)
    is AppError.Network -> stringResource(R.string.error_network)
    else -> stringResource(R.string.error_generic)
}

/**
 * Muestra [error] en un snackbar y avisa con [onShown] cuando el usuario lo ha visto
 * (o ha desaparecido) para que el store lo limpie.
 */
@Composable
fun ErrorSnackbarEffect(
    error: AppError?,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    val message = error?.asMessage()
    LaunchedEffect(error) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onShown()
        }
    }
}