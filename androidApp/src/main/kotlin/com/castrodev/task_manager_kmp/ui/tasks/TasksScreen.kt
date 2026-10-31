// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/tasks/TasksScreen.kt
package com.castrodev.task_manager_kmp.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.castrodev.task_manager_kmp.R

/** Pantalla provisional: se sustituye por la lista de tareas en el siguiente paso. */
@Composable
fun TasksScreen(onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.tasks_signed_in),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSignOut) {
                Text(stringResource(R.string.tasks_sign_out))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksScreenPreview() {
    MaterialTheme {
        TasksScreen(onSignOut = {})
    }
}