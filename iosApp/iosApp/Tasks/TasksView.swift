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
                    viewModel.openCreateForm()
                } label: {
                    Image(systemName: "plus")
                }
                .accessibilityLabel("Nueva tarea")
                .accessibilityIdentifier("tasks_add_button")
            }
        }
        .task { viewModel.refresh() }
        .sheet(
            isPresented: Binding(
                get: { viewModel.state.isCreateFormOpen },
                set: { isPresented in
                    if !isPresented { viewModel.closeCreateForm() }
                }
            )
        ) {
            CreateTaskSheet(
                isCreating: viewModel.state.isCreating,
                error: viewModel.state.createError,
                onCancel: viewModel.closeCreateForm,
                onCreate: { title, priority in viewModel.create(title: title, priority: priority) }
            )
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
        } else if let loadError = state.loadError, state.allTasks.isEmpty {
            RefreshableMessage(onRefresh: { await viewModel.refreshAndWait() }) {
                LoadErrorView(
                    title: "No se han podido cargar las tareas",
                    error: loadError,
                    onRetry: viewModel.refresh
                )
            }
        } else if state.visibleTasks.isEmpty {
            RefreshableMessage(onRefresh: { await viewModel.refreshAndWait() }) {
                ContentUnavailableView(
                    "No hay tareas para mostrar",
                    systemImage: "checklist"
                )
            }
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
            .refreshable { await viewModel.refreshAndWait() }
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

    let isCreating: Bool
    let error: (any AppError)?
    let onCancel: () -> Void
    let onCreate: (String, TaskPriority) -> Void
    @State private var title = ""
    @State private var priority: TaskPriority = .medium

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("Título", text: $title)
                        .disabled(isCreating)
                        .accessibilityIdentifier("task_title_field")
                } header: {
                    Text("Título")
                } footer: {
                    if let error {
                        Text(userMessage(for: error))
                            .foregroundStyle(.red)
                            .accessibilityIdentifier("task_create_error")
                    }
                }
                Section("Prioridad") {
                    Picker("Prioridad", selection: $priority) {
                        ForEach(allPriorities, id: \.self) { option in
                            Text(option.label).tag(option)
                        }
                    }
                    .pickerStyle(.segmented)
                    .disabled(isCreating)
                }
            }
            .interactiveDismissDisabled(isCreating)
            .navigationTitle("Nueva tarea")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar", action: onCancel)
                        .disabled(isCreating)
                }
                ToolbarItem(placement: .confirmationAction) {
                    if isCreating {
                        ProgressView()
                    } else {
                        Button("Crear") { onCreate(title, priority) }
                        .accessibilityIdentifier("task_create_button")
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }
}