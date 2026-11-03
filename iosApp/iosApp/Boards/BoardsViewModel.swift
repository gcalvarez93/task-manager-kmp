// Path: iosApp/iosApp/Boards/BoardsViewModel.swift
import Observation
import SharedLogic

/// ViewModel fino: la lógica vive en el `BoardListStore` compartido.
@MainActor
@Observable
final class BoardsViewModel {

    private(set) var state: BoardListState

    @ObservationIgnored private let store: BoardListStore

    // `nonisolated(unsafe)`: solo se tocan en `init` y en `deinit`, que no está aislado al hilo principal.
    @ObservationIgnored private nonisolated(unsafe) let scope = ScreenScope()
    @ObservationIgnored private nonisolated(unsafe) var observation: Task<Void, Never>?

    init(container: AppContainer) {
        store = container.createBoardListStore(scope: scope)
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

    func create(name: String, color: String) {
        store.create(board: NewBoard(name: name, description: "", color: color))
    }

    func delete(id: String) {
        store.delete(id: id)
    }

    func clearError() {
        store.clearError()
    }
}