// Path: iosApp/iosApp/Tasks/TasksViewModel.swift
import Observation
import SharedLogic

/// ViewModel fino: la lógica vive en el `TaskListStore` compartido, filtrado por tablero.
@MainActor
@Observable
final class TasksViewModel {

    private(set) var state: TaskListState

    @ObservationIgnored private let boardId: String
    @ObservationIgnored private let store: TaskListStore

    // `nonisolated(unsafe)`: solo se tocan en `init` y en `deinit`, que no está aislado al hilo principal.
    @ObservationIgnored private nonisolated(unsafe) let scope = ScreenScope()
    @ObservationIgnored private nonisolated(unsafe) var observation: Task<Void, Never>?

    init(container: AppContainer, boardId: String) {
        self.boardId = boardId
        store = container.createTaskListStore(boardId: boardId, scope: scope)
        state = store.state.value
        observation = Task { [weak self, store] in
            for await newState in store.state {
                self?.state = newState
            }
        }
    }

    deinit {
        observation?.cancel()
        scope.close()
    }

    func refresh() {
        store.refresh()
    }

    func setFilter(_ status: TaskStatus?) {
        store.setFilter(status: status)
    }

    func changeStatus(of task: TaskItem, to status: TaskStatus) {
        store.changeStatus(task: task, status: status)
    }

    func delete(id: String) {
        store.delete(id: id)
    }

    func create(title: String, priority: TaskPriority) {
        store.create(
            task: NewTask(
                boardId: boardId,
                title: title,
                description: "",
                priority: priority,
                status: .pending,
                dueDate: nil,
                labels: []
            )
        )
    }

    func clearError() {
        store.clearError()
    }
}