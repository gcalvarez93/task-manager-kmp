// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/boards/BoardsScreen.kt
package com.castrodev.task_manager_kmp.ui.boards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.R
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.domain.model.Board
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.presentation.boards.BoardListState
import com.castrodev.task_manager_kmp.ui.common.ErrorSnackbarEffect
import com.castrodev.task_manager_kmp.ui.common.FullScreenMessage
import com.castrodev.task_manager_kmp.ui.common.LoadErrorMessage
import com.castrodev.task_manager_kmp.ui.common.asMessage
import kotlin.time.Instant

private val BoardColors = listOf("#FF9800", "#F44336", "#4CAF50", "#2196F3", "#9C27B0")

/** Conecta el [BoardListViewModel] con la pantalla. */
@Composable
fun BoardsRoute(
    container: AppContainer,
    onBoardClick: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    val viewModel = viewModel { BoardListViewModel(container) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    BoardsScreen(
        state = state,
        onBoardClick = onBoardClick,
        onRefresh = viewModel::refresh,
        onOpenCreateForm = viewModel::openCreateForm,
        onCloseCreateForm = viewModel::closeCreateForm,
        onCreate = viewModel::create,
        onDelete = viewModel::delete,
        onErrorShown = viewModel::clearError,
        onSignOut = onSignOut,
    )
}

/**
 * Pantalla sin estado propio salvo lo que se escribe en el formulario y el dialogo de borrar.
 * El formulario de crear esta abierto mientras [BoardListState.isCreateFormOpen] sea `true`:
 * si crear falla sigue abierto y conserva lo escrito.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardsScreen(
    state: BoardListState,
    onBoardClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenCreateForm: () -> Unit,
    onCloseCreateForm: () -> Unit,
    onCreate: (NewBoard) -> Unit,
    onDelete: (String) -> Unit,
    onErrorShown: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var boardToDelete by remember { mutableStateOf<Board?>(null) }
    // Copia local: `state` viene de otro modulo y Kotlin no permite hacer smart cast sobre sus propiedades.
    val loadError = state.loadError

    ErrorSnackbarEffect(state.error, snackbarHostState, onErrorShown)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.boards_title)) },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text(stringResource(R.string.boards_sign_out))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenCreateForm) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading && state.boards.isNotEmpty(),
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading && state.boards.isEmpty() -> Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                loadError != null && state.boards.isEmpty() -> LoadErrorMessage(
                    error = loadError,
                    onRetry = onRefresh,
                )
                state.boards.isEmpty() -> FullScreenMessage(
                    text = stringResource(R.string.boards_empty),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    items(state.boards, key = { it.id }) { board ->
                        BoardCard(
                            board = board,
                            onClick = { onBoardClick(board.id) },
                            onDelete = { boardToDelete = board },
                        )
                    }
                }
            }
        }
    }

    if (state.isCreateFormOpen) {
        CreateBoardDialog(
            isCreating = state.isCreating,
            error = state.createError,
            onDismiss = onCloseCreateForm,
            onCreate = onCreate,
        )
    }

    boardToDelete?.let { board ->
        AlertDialog(
            onDismissRequest = { boardToDelete = null },
            title = { Text(stringResource(R.string.boards_delete_title)) },
            text = { Text(stringResource(R.string.boards_delete_message, board.name)) },
            confirmButton = {
                TextButton(onClick = {
                    boardToDelete = null
                    onDelete(board.id)
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { boardToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun BoardCard(board: Board, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(parseColor(board.color), CircleShape),
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(board.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = pluralStringResource(
                        R.plurals.boards_task_count,
                        board.taskCount,
                        board.taskCount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete)) }
        }
    }
}

@Composable
private fun CreateBoardDialog(
    isCreating: Boolean,
    error: AppError?,
    onDismiss: () -> Unit,
    onCreate: (NewBoard) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(BoardColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.boards_create_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.boards_name_label)) },
                    singleLine = true,
                    enabled = !isCreating,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.boards_color_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BoardColors.forEach { option ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(parseColor(option), CircleShape)
                                .border(
                                    width = if (option == color) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape,
                                )
                                .clickable(enabled = !isCreating) { color = option },
                        )
                    }
                }
                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = error.asMessage(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(NewBoard(name = name, color = color)) },
                enabled = !isCreating,
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.action_create))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/** Convierte `#RRGGBB` en [Color]; si la API devuelve algo raro usa gris en vez de romper la pantalla. */
internal fun parseColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)

@Preview(showBackground = true)
@Composable
private fun BoardsScreenPreview() {
    MaterialTheme {
        BoardsScreen(
            state = BoardListState(
                boards = listOf(
                    Board("1", "Casa", "", "#FF9800", 3, Instant.fromEpochMilliseconds(0)),
                    Board("2", "Trabajo", "", "#2196F3", 1, Instant.fromEpochMilliseconds(0)),
                ),
            ),
            onBoardClick = {},
            onRefresh = {},
            onOpenCreateForm = {},
            onCloseCreateForm = {},
            onCreate = {},
            onDelete = {},
            onErrorShown = {},
            onSignOut = {},
        )
    }
}