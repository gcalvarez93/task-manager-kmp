// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/tasks/TasksScreen.kt
package com.castrodev.task_manager_kmp.ui.tasks

import androidx.annotation.StringRes
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.R
import com.castrodev.task_manager_kmp.core.AppError
import com.castrodev.task_manager_kmp.domain.model.NewTask
import com.castrodev.task_manager_kmp.domain.model.TaskItem
import com.castrodev.task_manager_kmp.domain.model.TaskPriority
import com.castrodev.task_manager_kmp.domain.model.TaskStatus
import com.castrodev.task_manager_kmp.presentation.tasks.TaskListState
import com.castrodev.task_manager_kmp.ui.common.ErrorSnackbarEffect
import com.castrodev.task_manager_kmp.ui.common.FullScreenMessage
import com.castrodev.task_manager_kmp.ui.common.LoadErrorMessage
import com.castrodev.task_manager_kmp.ui.common.asMessage
import kotlin.time.Instant

@StringRes
private fun TaskStatus.label(): Int = when (this) {
    TaskStatus.PENDING -> R.string.status_pending
    TaskStatus.IN_PROGRESS -> R.string.status_in_progress
    TaskStatus.DONE -> R.string.status_done
}

@StringRes
private fun TaskPriority.label(): Int = when (this) {
    TaskPriority.LOW -> R.string.priority_low
    TaskPriority.MEDIUM -> R.string.priority_medium
    TaskPriority.HIGH -> R.string.priority_high
}

/** Conecta el [TaskListViewModel] de un tablero con la pantalla. */
@Composable
fun TasksRoute(container: AppContainer, boardId: String, onBack: () -> Unit) {
    val viewModel = viewModel(key = "tasks-$boardId") { TaskListViewModel(container, boardId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    TasksScreen(
        state = state,
        boardId = boardId,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onFilterChange = viewModel::setFilter,
        onChangeStatus = viewModel::changeStatus,
        onDelete = viewModel::delete,
        onOpenCreateForm = viewModel::openCreateForm,
        onCloseCreateForm = viewModel::closeCreateForm,
        onCreate = viewModel::create,
        onErrorShown = viewModel::clearError,
    )
}

/**
 * Pantalla de las tareas de un tablero. Solo guarda lo que se escribe en el formulario y el dialogo de borrar.
 * El formulario de crear esta abierto mientras [TaskListState.isCreateFormOpen] sea `true`:
 * si crear falla sigue abierto y conserva lo escrito.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    state: TaskListState,
    boardId: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFilterChange: (TaskStatus?) -> Unit,
    onChangeStatus: (TaskItem, TaskStatus) -> Unit,
    onDelete: (String) -> Unit,
    onOpenCreateForm: () -> Unit,
    onCloseCreateForm: () -> Unit,
    onCreate: (NewTask) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }
    // Copia local: `state` viene de otro modulo y Kotlin no permite hacer smart cast sobre sus propiedades.
    val loadError = state.loadError

    ErrorSnackbarEffect(state.error, snackbarHostState, onErrorShown)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasks_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.tasks_back)) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            FilterRow(selected = state.filter, onSelected = onFilterChange)

            PullToRefreshBox(
                isRefreshing = state.isLoading && state.allTasks.isNotEmpty(),
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading && state.allTasks.isEmpty() -> Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    loadError != null && state.allTasks.isEmpty() -> LoadErrorMessage(
                        error = loadError,
                        onRetry = onRefresh,
                    )
                    state.visibleTasks.isEmpty() -> FullScreenMessage(
                        text = stringResource(R.string.tasks_empty),
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        items(state.visibleTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onChangeStatus = { onChangeStatus(task, it) },
                                onDelete = { taskToDelete = task },
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.isCreateFormOpen) {
        CreateTaskDialog(
            isCreating = state.isCreating,
            error = state.createError,
            onDismiss = onCloseCreateForm,
            onCreate = { title, priority ->
                onCreate(NewTask(boardId = boardId, title = title, priority = priority))
            },
        )
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text(stringResource(R.string.tasks_delete_title)) },
            text = { Text(stringResource(R.string.tasks_delete_message, task.title)) },
            confirmButton = {
                TextButton(onClick = {
                    taskToDelete = null
                    onDelete(task.id)
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun FilterRow(selected: TaskStatus?, onSelected: (TaskStatus?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text(stringResource(R.string.tasks_filter_all)) },
        )
        TaskStatus.entries.forEach { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelected(status) },
                label = { Text(stringResource(status.label())) },
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    onChangeStatus: (TaskStatus) -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.tasks_priority_label) + ": " +
                        stringResource(task.priority.label()),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    TextButton(onClick = { menuExpanded = true }) {
                        Text(
                            stringResource(R.string.tasks_status_label) + ": " +
                                    stringResource(task.status.label()),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        TaskStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(stringResource(status.label())) },
                                onClick = {
                                    menuExpanded = false
                                    if (status != task.status) onChangeStatus(status)
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete)) }
            }
        }
    }
}

@Composable
private fun CreateTaskDialog(
    isCreating: Boolean,
    error: AppError?,
    onDismiss: () -> Unit,
    onCreate: (title: String, priority: TaskPriority) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tasks_create_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.tasks_title_label)) },
                    singleLine = true,
                    enabled = !isCreating,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.tasks_priority_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { option ->
                        FilterChip(
                            selected = option == priority,
                            onClick = { priority = option },
                            enabled = !isCreating,
                            label = { Text(stringResource(option.label())) },
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
            TextButton(onClick = { onCreate(title, priority) }, enabled = !isCreating) {
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

@Preview(showBackground = true)
@Composable
private fun TasksScreenPreview() {
    val task = TaskItem(
        id = "1",
        boardId = "b1",
        title = "Comprar pan",
        description = "",
        status = TaskStatus.PENDING,
        priority = TaskPriority.HIGH,
        dueDate = null,
        labels = emptyList(),
        createdAt = Instant.fromEpochMilliseconds(0),
    )
    MaterialTheme {
        TasksScreen(
            state = TaskListState(allTasks = listOf(task), visibleTasks = listOf(task)),
            boardId = "b1",
            onBack = {},
            onRefresh = {},
            onFilterChange = {},
            onChangeStatus = { _, _ -> },
            onDelete = {},
            onOpenCreateForm = {},
            onCloseCreateForm = {},
            onCreate = {},
            onErrorShown = {},
        )
    }
}