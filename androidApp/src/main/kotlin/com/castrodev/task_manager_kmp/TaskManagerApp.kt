// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/TaskManagerApp.kt
package com.castrodev.task_manager_kmp

import android.app.Application
import com.google.firebase.auth.FirebaseAuth

/**
 * Clase Application: crea el [AppContainer] una sola vez para toda la app.
 * Es una inyeccion de dependencias manual, suficiente para el tamano de esta app.
 */
class TaskManagerApp : Application() {

    val container: AppContainer by lazy {
        AppContainer(
            baseUrl = BuildConfig.API_BASE_URL,
            tokenProvider = FirebaseTokenProvider(FirebaseAuth.getInstance()),
        )
    }
}