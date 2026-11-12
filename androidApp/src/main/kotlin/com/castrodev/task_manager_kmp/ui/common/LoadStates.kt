// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/common/LoadStates.kt
package com.castrodev.task_manager_kmp.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.castrodev.task_manager_kmp.R
import com.castrodev.task_manager_kmp.core.AppError

/**
 * Mensaje centrado que ocupa toda la pantalla. Es desplazable a proposito: asi el gesto de
 * "tirar para recargar" tambien funciona cuando no hay lista (vacia o con error).
 */
@Composable
fun FullScreenMessage(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp)) {
                Text(actionLabel)
            }
        }
    }
}

/** La carga ha fallado y no hay nada que mostrar: se explica el motivo y se ofrece reintentar. */
@Composable
fun LoadErrorMessage(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    FullScreenMessage(
        text = error.asMessage(),
        modifier = modifier,
        actionLabel = stringResource(R.string.action_retry),
        onAction = onRetry,
    )
}