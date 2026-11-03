// Path: iosApp/iosApp/Tasks/TasksView.swift
import SharedLogic
import SwiftUI

private let allStatuses: [TaskStatus] = [.pending, .inProgress, .done]
private let allPriorities: [TaskPriority] = [.low, .medium, .high]

private extension TaskStatus {
    var label: String {
        switch self {
        case .pending: "Pendiente"
        case .inProgress: "En curso"
        case .done: "Hecha"
        }
    }
}

private extension TaskPriority {
    var label: String {
        switch self {
        case .low: "Baja"
        case .medium: "Media"
        case .high: "Alta"
        }
    }
}

/// Tareas de un tablero. Conecta el `TasksViewModel` con la pantalla.
struct TasksView: View {

    let title: String
    @State private var viewModel: TasksViewModel
    @State private var showCreateSheet = false
    @State private var taskToDelete: TaskItem?

    init(container: AppContainer, boardId: String, title: String) {
        self.title = title
        _viewModel = State(initialValue: TasksViewModel(container: container, boardId: boardId))
    }

    var body: some View {
        let state = viewModel.state
        VStack(spacing: 0) {
            Picker(
                "Estado",
                selection: Binding(
                    get: { state.filter },
                    set: { viewModel.setFilter($0) }
                )
            ) {
                Text("Todas").tag(TaskStatus?.none)
                ForEach(allStatuses, id: \.self) { status in
                    Text(status.label).tag(Optional(status))
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)
            .padding(.vertical, 8)

            content(state)
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showCreateSheet = true
                } label: {
                    Image(systemName: "plus")
                }
                .accessibilityLabel("Nueva tarea")
                .accessibilityIdentifier("tasks_add_button")
            }
        }
        .task { viewModel.refresh() }
        .sheet(isPresented: $showCreateSheet) {
            CreateTaskSheet { title, priority in
                viewModel.create(title: title, priority: priority)
            }
        }
        .confirmationDialog(
            "Eliminar tarea",
            isPresented: Binding(
                get: { taskToDelete != nil },
                set: { isPresented in
                    if !isPresented { taskToDelete = nil }
                }
            ),
            titleVisibility: .visible,
            presenting: taskToDelete
        ) { task in
            Button("Eliminar", role: .destructive) {
                viewModel.delete(id: task.id)
            }
        } message: { task in
            Text("Se eliminará la tarea «\(task.title)». Esta acción no se puede deshacer.")
        }
        .errorAlert(state.error, onDismiss: viewModel.clearError)
    }

    @ViewBuilder
    private func content(_ state: TaskListState) -> some View {
        if state.isLoading && state.allTasks.isEmpty {
            ProgressView()
                .frame(maxHeight: .infinity)
        } else if state.visibleTasks.isEmpty {
            ContentUnavailableView(
                "No hay tareas para mostrar",
                systemImage: "checklist"
            )
        } else {
            List(state.visibleTasks, id: \.id) { task in
                TaskRow(
                    task: task,
                    onChangeStatus: { viewModel.changeStatus(of: task, to: $0) }
                )
                .swipeActions {
                    Button(role: .destructive) {
                        taskToDelete = task
                    } label: {
                        Label("Eliminar", systemImage: "trash")
                    }
                }
            }
        }
    }
}

private struct TaskRow: View {

    let task: TaskItem
    let onChangeStatus: (TaskStatus) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(task.title)
                .font(.headline)
            Text("Prioridad: \(task.priority.label)")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Menu {
                ForEach(allStatuses, id: \.self) { status in
                    Button(status.label) {
                        if status != task.status { onChangeStatus(status) }
                    }
                }
            } label: {
                Text("Estado: \(task.status.label)")
                    .font(.subheadline)
            }
        }
        .padding(.vertical, 4)
    }
}

private struct CreateTaskSheet: View {

    let onCreate: (String, TaskPriority) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var priority: TaskPriority = .medium

    var body: some View {
        NavigationStack {
            Form {
                Section("Título") {
                    TextField("Título", text: $title)
                        .accessibilityIdentifier("task_title_field")
                }
                Section("Prioridad") {
                    Picker("Prioridad", selection: $priority) {
                        ForEach(allPriorities, id: \.self) { option in
                            Text(option.label).tag(option)
                        }
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Nueva tarea")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Crear") {
                        onCreate(title, priority)
                        dismiss()
                    }
                    .accessibilityIdentifier("task_create_button")
                }
            }
        }
        .presentationDetents([.medium])
    }
}