// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/TaskManagerApp.kt
package com.castrodev.task_manager_kmp

import android.app.Application
import com.castrodev.task_manager_kmp.logging.TimberErrorLogger
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

/**
 * Clase Application: crea el [AppContainer] una sola vez para toda la app.
 * Es una inyeccion de dependencias manual, suficiente para el tamano de esta app.
 */
class TaskManagerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Solo se escribe en logcat en debug: en release no hay ningun Tree y los logs se descartan.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    val container: AppContainer by lazy {
        val firebaseAuth = FirebaseAuth.getInstance()
        AppContainer(
            baseUrl = BuildConfig.API_BASE_URL,
            tokenProvider = FirebaseTokenProvider(firebaseAuth),
            authRepository = FirebaseAuthRepository(firebaseAuth),
            logger = TimberErrorLogger(),
        )
    }
}