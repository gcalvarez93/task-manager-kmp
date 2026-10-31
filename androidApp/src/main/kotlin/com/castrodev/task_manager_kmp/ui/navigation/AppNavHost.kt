// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/navigation/AppNavHost.kt
package com.castrodev.task_manager_kmp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.ui.boards.BoardsRoute
import com.castrodev.task_manager_kmp.ui.login.LoginRoute
import com.castrodev.task_manager_kmp.ui.tasks.TasksRoute

private const val ROUTE_LOGIN = "login"
private const val ROUTE_BOARDS = "boards"
private const val ARG_BOARD_ID = "boardId"
private const val ROUTE_TASKS = "tasks/{$ARG_BOARD_ID}"

private fun tasksRoute(boardId: String) = "tasks/$boardId"

/**
 * Navegacion de la app: login -> tableros -> tareas de un tablero.
 * Arranca en tableros si ya hay sesion. Al iniciar o cerrar sesion se elimina la otra
 * parte de la pila, de modo que "atras" nunca devuelve al login con la sesion abierta.
 */
@Composable
fun AppNavHost(
    container: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val startDestination = remember {
        if (container.authRepository.isSignedIn) ROUTE_BOARDS else ROUTE_LOGIN
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ROUTE_LOGIN) {
            LoginRoute(
                container = container,
                onSignedIn = {
                    navController.navigate(ROUTE_BOARDS) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(ROUTE_BOARDS) {
            BoardsRoute(
                container = container,
                onBoardClick = { boardId -> navController.navigate(tasksRoute(boardId)) },
                onSignOut = {
                    container.authRepository.signOut()
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_BOARDS) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = ROUTE_TASKS,
            arguments = listOf(navArgument(ARG_BOARD_ID) { type = NavType.StringType }),
        ) { entry ->
            val boardId = entry.arguments?.getString(ARG_BOARD_ID)
            if (boardId != null) {
                TasksRoute(
                    container = container,
                    boardId = boardId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}