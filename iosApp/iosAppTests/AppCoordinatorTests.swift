// Path: iosApp/iosAppTests/AppCoordinatorTests.swift
import SharedLogic
import Testing
@testable import TaskManagerKMP

@MainActor
struct AppCoordinatorTests {

    private func board(id: String, name: String) -> Board {
        Board(
            id: id,
            name: name,
            description: "",
            color: "#FF9800",
            taskCount: 0,
            createdAt: KotlinInstant.Companion.shared.fromEpochMilliseconds(epochMilliseconds: 0)
        )
    }

    @Test func startsWithAnEmptyPath() {
        let coordinator = AppCoordinator()

        #expect(coordinator.path.isEmpty)
    }

    @Test func showTasksPushesTheBoardDestination() {
        let coordinator = AppCoordinator()

        coordinator.showTasks(of: board(id: "b1", name: "Casa"))

        #expect(coordinator.path == [.tasks(boardId: "b1", boardName: "Casa")])
    }

    @Test func showTasksStacksDestinations() {
        let coordinator = AppCoordinator()

        coordinator.showTasks(of: board(id: "b1", name: "Casa"))
        coordinator.showTasks(of: board(id: "b2", name: "Trabajo"))

        #expect(coordinator.path.count == 2)
        #expect(coordinator.path.last == .tasks(boardId: "b2", boardName: "Trabajo"))
    }
}