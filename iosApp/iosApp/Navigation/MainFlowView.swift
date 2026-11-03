// Path: iosApp/iosApp/Navigation/MainFlowView.swift
import SharedLogic
import SwiftUI

/// Flujo principal con sesión iniciada: tableros -> tareas de un tablero.
struct MainFlowView: View {

    let container: AppContainer
    let onSignOut: () -> Void
    @State private var coordinator = AppCoordinator()

    var body: some View {
        @Bindable var coordinator = coordinator
        NavigationStack(path: $coordinator.path) {
            BoardsView(
                container: container,
                onBoardTap: { board in coordinator.showTasks(of: board) },
                onSignOut: onSignOut
            )
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case let .tasks(boardId, boardName):
                    TasksView(container: container, boardId: boardId, title: boardName)
                }
            }
        }
    }
}