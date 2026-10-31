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

    /** Carga los tableros. Si ya hay una carga en curso, no hace nada. */
    fun refresh() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            when (val result = repository.getBoards()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, boards = result.value) }
                is AppResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }

    fun create(board: NewBoard) {
        scope.launch {
            when (val result = createBoardUseCase(board)) {
                is AppResult.Success -> _state.update { it.copy(boards = it.boards + result.value) }
                is AppResult.Failure -> _state.update { it.copy(error = result.error) }
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

    /** La vista llama a esto despues de mostrar el error. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}