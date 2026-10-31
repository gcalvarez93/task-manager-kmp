// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/login/LoginViewModel.kt
package com.castrodev.task_manager_kmp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.presentation.login.LoginState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel fino: la logica vive en el `LoginStore` compartido, que aqui solo recibe
 * el `viewModelScope` para que su trabajo se cancele cuando la pantalla desaparece.
 */
class LoginViewModel(container: AppContainer) : ViewModel() {

    private val store = container.createLoginStore(viewModelScope)

    val state: StateFlow<LoginState> = store.state

    fun onEmailChange(email: String) = store.onEmailChange(email)

    fun onPasswordChange(password: String) = store.onPasswordChange(password)

    fun submit() = store.submit()
}