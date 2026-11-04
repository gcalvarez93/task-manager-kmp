// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/login/LoginScreen.kt
package com.castrodev.task_manager_kmp.ui.login

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.R
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.presentation.login.AuthMode
import com.castrodev.task_manager_kmp.presentation.login.EmailError
import com.castrodev.task_manager_kmp.presentation.login.LoginState
import com.castrodev.task_manager_kmp.presentation.login.MIN_PASSWORD_LENGTH
import com.castrodev.task_manager_kmp.presentation.login.PasswordError

private val HeaderHeight: Dp = 280.dp
private val CardTopOffset: Dp = 216.dp
private val FieldShape = RoundedCornerShape(14.dp)

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
        onToggleMode = viewModel::toggleMode,
        onSubmit = viewModel::submit,
    )
}

/**
 * Pantalla sin estado propio: solo pinta [state] y devuelve los eventos del usuario.
 * Sirve para iniciar sesion y para registrarse segun [LoginState.mode].
 */
@Composable
fun LoginScreen(
    state: LoginState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val isSignUp = state.mode == AuthMode.SIGN_UP

    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }

    // Sin insets en el Scaffold: la cabecera con degradado debe llegar hasta la barra de estado.
    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0.dp)) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LoginHeader(
                title = stringResource(if (isSignUp) R.string.register_title else R.string.login_title),
                subtitle = stringResource(if (isSignUp) R.string.register_subtitle else R.string.login_subtitle),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(CardTopOffset))

                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(24.dp),
                    ) {
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = { Text(stringResource(R.string.login_email)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            enabled = !state.isLoading,
                            isError = state.emailError != null,
                            supportingText = {
                                state.emailError?.let { Text(stringResource(it.messageRes())) }
                            },
                            shape = FieldShape,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = { Text(stringResource(R.string.login_password)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(
                                        stringResource(
                                            if (passwordVisible) R.string.login_hide_password
                                            else R.string.login_show_password,
                                        ),
                                    )
                                }
                            },
                            singleLine = true,
                            enabled = !state.isLoading,
                            isError = state.passwordError != null,
                            supportingText = {
                                val passwordError = state.passwordError
                                if (passwordError != null) {
                                    Text(passwordError.message())
                                } else if (isSignUp) {
                                    Text(stringResource(R.string.login_password_hint, MIN_PASSWORD_LENGTH))
                                }
                            },
                            shape = FieldShape,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        state.error?.let { error ->
                            Spacer(Modifier.height(12.dp))
                            ErrorBanner(text = stringResource(error.messageRes()))
                        }

                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { submit() },
                            enabled = !state.isLoading,
                            shape = FieldShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp,
                                    color = LocalContentColor.current,
                                )
                            } else {
                                Text(
                                    text = stringResource(
                                        if (isSignUp) R.string.register_submit else R.string.login_submit,
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                ModeSwitch(isSignUp = isSignUp, enabled = !state.isLoading, onClick = onToggleMode)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/** Cabecera de marca: degradado, insignia y titulo. El card del formulario se solapa con ella. */
@Composable
private fun LoginHeader(title: String, subtitle: String) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeight)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.verticalGradient(listOf(colors.primary, colors.tertiary)))
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 56.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.onPrimary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onPrimary.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
    }
}

/** Enlace inferior para alternar entre iniciar sesion y crear cuenta. */
@Composable
private fun ModeSwitch(isSignUp: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(if (isSignUp) R.string.login_have_account else R.string.login_no_account),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onClick, enabled = enabled) {
            Text(
                text = stringResource(
                    if (isSignUp) R.string.login_switch_to_sign_in else R.string.login_switch_to_sign_up,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ErrorBanner(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@StringRes
private fun EmailError.messageRes(): Int = when (this) {
    EmailError.EMPTY -> R.string.login_error_email_empty
    EmailError.INVALID -> R.string.login_error_email_invalid
}

/** Composable porque el mensaje de contrasena corta lleva la longitud minima como argumento. */
@Composable
private fun PasswordError.message(): String = when (this) {
    PasswordError.EMPTY -> stringResource(R.string.login_error_password_empty)
    PasswordError.TOO_SHORT -> stringResource(R.string.login_error_password_too_short, MIN_PASSWORD_LENGTH)
}

@StringRes
private fun AppError.messageRes(): Int = when (this) {
    AppError.Unauthorized -> R.string.login_error_credentials
    AppError.EmailAlreadyInUse -> R.string.login_error_email_in_use
    AppError.WeakPassword -> R.string.login_error_weak_password
    is AppError.Validation -> R.string.login_error_email_invalid
    is AppError.Network -> R.string.login_error_network
    else -> R.string.login_error_generic
}

@Preview(showBackground = true, name = "Login con error")
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            state = LoginState(email = "ana@example.com", error = AppError.Unauthorized),
            onEmailChange = {},
            onPasswordChange = {},
            onToggleMode = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, name = "Registro con error")
@Composable
private fun RegisterScreenPreview() {
    MaterialTheme {
        LoginScreen(
            state = LoginState(
                mode = AuthMode.SIGN_UP,
                email = "ana@example.com",
                error = AppError.EmailAlreadyInUse,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onToggleMode = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, name = "Registro oscuro cargando", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RegisterScreenDarkLoadingPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LoginScreen(
            state = LoginState(mode = AuthMode.SIGN_UP, email = "ana@example.com", isLoading = true),
            onEmailChange = {},
            onPasswordChange = {},
            onToggleMode = {},
            onSubmit = {},
        )
    }
}