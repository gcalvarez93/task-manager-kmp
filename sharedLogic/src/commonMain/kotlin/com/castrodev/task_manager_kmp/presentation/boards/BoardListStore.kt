// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/presentation/boards/BoardListStore.kt
package com.castrodev.task_manager_kmp.presentation.boards

import com.castrodev.task_manager_kmp.core.AppResult
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.domain.repository.BoardRepository
import com.castrodev.task_manager_kmp.domain.usecase.CreateBoardUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Logica de presentacion compartida de la lista de tableros.
 * Igual que `TaskListStore`, el [scope] lo pone cada plataforma.
 */
class BoardListStore(
    private val repository: BoardRepository,
    private val createBoardUseCase: CreateBoardUseCase,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(BoardListState())
    val state: StateFlow<BoardListState> = _state.asStateFlow()

    /**
     * Carga los tableros. Si ya hay una carga en curso, no hace nada.
     * Si falla y no hay nada que mostrar, queda en [BoardListState.loadError] (la pantalla ofrece reintentar);
     * si falla con datos ya en pantalla, estos se conservan y el fallo se avisa en [BoardListState.error].
     */
    fun refresh() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null, loadError = null) }
        scope.launch {
            when (val result = repository.getBoards()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, boards = result.value) }
                is AppResult.Failure -> _state.update { current ->
                    if (current.boards.isEmpty()) {
                        current.copy(isLoading = false, loadError = result.error)
                    } else {
                        current.copy(isLoading = false, error = result.error)
                    }
                }
            }
        }
    }

    /** Abre el formulario de crear tablero. */
    fun openCreateForm() {
        _state.update { it.copy(isCreateFormOpen = true, createError = null) }
    }

    /** Cierra el formulario y descarta su error. Se ignora mientras hay un intento de crear en curso. */
    fun closeCreateForm() {
        _state.update { if (it.isCreating) it else it.copy(isCreateFormOpen = false, createError = null) }
    }

    /**
     * Crea un tablero. Si sale bien, lo anade a la lista y cierra el formulario; si falla (validacion o API),
     * el formulario sigue abierto y el motivo queda en [BoardListState.createError].
     * Se ignora si ya hay un intento en curso.
     */
    fun create(board: NewBoard) {
        if (_state.value.isCreating) return
        _state.update { it.copy(isCreating = true, createError = null) }
        scope.launch {
            when (val result = createBoardUseCase(board)) {
                is AppResult.Success -> _state.update {
                    it.copy(isCreating = false, isCreateFormOpen = false, boards = it.boards + result.value)
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isCreating = false, createError = result.error)
                }
            }
        }
    }

    fun delete(id: String) {
        scope.launch {
            when (val result = repository.deleteBoard(id)) {
                is AppResult.Success -> _state.update { current ->
                    current.copy(boards = current.boards.filterNot { it.id == id })
                }
                is AppResult.Failure -> _state.update { it.copy(error = result.error) }
            }
        }
    }

    /** La vista llama a esto despues de mostrar el aviso. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}