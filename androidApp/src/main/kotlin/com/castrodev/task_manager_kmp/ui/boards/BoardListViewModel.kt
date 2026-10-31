// Path: androidApp/src/main/kotlin/com/castrodev/task_manager_kmp/ui/boards/BoardListViewModel.kt
package com.castrodev.task_manager_kmp.ui.boards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.castrodev.task_manager_kmp.AppContainer
import com.castrodev.task_manager_kmp.domain.model.NewBoard
import com.castrodev.task_manager_kmp.presentation.boards.BoardListState
import kotlinx.coroutines.flow.StateFlow

/** ViewModel fino: la logica vive en el `BoardListStore` compartido. */
class BoardListViewModel(container: AppContainer) : ViewModel() {

    private val store = container.createBoardListStore(viewModelScope)

    val state: StateFlow<BoardListState> = store.state

    init {
        store.refresh()
    }

    fun refresh() = store.refresh()

    fun create(board: NewBoard) = store.create(board)

    fun delete(id: String) = store.delete(id)

    fun clearError() = store.clearError()
}