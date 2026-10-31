// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/navigation/AppNavHost.kt
package com.castrodev.task_manager_kmp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.ui.login.LoginRoute
import com.castrodev.task_manager_kmp.ui.tasks.TasksScreen

private const val ROUTE_LOGIN = "login"
private const val ROUTE_TASKS = "tasks"

/**
 * Navegacion de la app. Arranca en la lista de tareas si ya hay sesion, o en el login si no.
 * Al iniciar o cerrar sesion se elimina la otra pantalla de la pila, de modo que
 * "atras" nunca devuelve al login con la sesion abierta.
 */
@Composable
fun AppNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val startDestination = remember {
        if (container.authRepository.isSignedIn) ROUTE_TASKS else ROUTE_LOGIN
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ROUTE_LOGIN) {
            LoginRoute(
                container = container,
                onSignedIn = {
                    navController.navigate(ROUTE_TASKS) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(ROUTE_TASKS) {
            TasksScreen(
                onSignOut = {
                    container.authRepository.signOut()
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_TASKS) { inclusive = true }
                    }
                },
            )
        }
    }
}