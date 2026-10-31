// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/login/LoginScreen.kt
package com.castrodev.task_manager_kmp.ui.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.R
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.presentation.login.EmailError
import com.castrodev.task_manager_kmp.presentation.login.LoginState
import com.castrodev.task_manager_kmp.presentation.login.PasswordError

/** Conecta el [LoginViewModel] con la pantalla y avisa cuando se ha iniciado sesion. */
@Composable
fun LoginRoute(container: AppContainer, onSignedIn: () -> Unit) {
    val viewModel = viewModel { LoginViewModel(container) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) onSignedIn()
    }

    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
    )
}

/** Pantalla sin estado propio: solo pinta [state] y devuelve los eventos del usuario. */
@Composable
fun LoginScreen(
    state: LoginState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.login_email)) },
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.emailError != null,
                supportingText = {
                    state.emailError?.let { Text(stringResource(it.messageRes())) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.login_password)) },
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.passwordError != null,
                supportingText = {
                    state.passwordError?.let { Text(stringResource(it.messageRes())) }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )

            state.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(error.messageRes()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onSubmit,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.login_submit))
                }
            }
        }
    }
}

@StringRes
private fun EmailError.messageRes(): Int = when (this) {
    EmailError.EMPTY -> R.string.login_error_email_empty
    EmailError.INVALID -> R.string.login_error_email_invalid
}

@StringRes
private fun PasswordError.messageRes(): Int = when (this) {
    PasswordError.EMPTY -> R.string.login_error_password_empty
}

@StringRes
private fun AppError.messageRes(): Int = when (this) {
    AppError.Unauthorized -> R.string.login_error_credentials
    is AppError.Network -> R.string.login_error_network
    else -> R.string.login_error_generic
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            state = LoginState(email = "ana@example.com", error = AppError.Unauthorized),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
        )
    }
}