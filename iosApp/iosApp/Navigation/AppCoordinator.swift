// Path: iosApp/iosApp/Navigation/AppCoordinator.swift
import Observation
import SharedLogic

/// Destinos a los que se puede navegar desde la pantalla de tableros.
enum AppRoute: Hashable {
    case tasks(boardId: String, boardName: String)
}

/// Coordinador ligero: es el único que conoce la pila de navegación. Las pantallas
/// solo avisan de lo que ha pasado ("se ha elegido un tablero") y no construyen destinos.
@MainActor
@Observable
final class AppCoordinator {

    var path: [AppRoute] = []

    func showTasks(of board: Board) {
        path.append(.tasks(boardId: board.id, boardName: board.name))
    }
}