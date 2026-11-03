// Path: iosApp/iosApp/Boards/BoardsView.swift
import SharedLogic
import SwiftUI

/// Lista de tableros. Conecta el `BoardsViewModel` con la pantalla.
struct BoardsView: View {

    let onBoardTap: (Board) -> Void
    let onSignOut: () -> Void
    @State private var viewModel: BoardsViewModel
    @State private var showCreateSheet = false
    @State private var boardToDelete: Board?

    init(
        container: AppContainer,
        onBoardTap: @escaping (Board) -> Void,
        onSignOut: @escaping () -> Void
    ) {
        self.onBoardTap = onBoardTap
        self.onSignOut = onSignOut
        _viewModel = State(initialValue: BoardsViewModel(container: container))
    }

    var body: some View {
        let state = viewModel.state
        content(state)
            .navigationTitle("Tableros")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cerrar sesión", action: onSignOut)
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showCreateSheet = true
                    } label: {
                        Image(systemName: "plus")
                    }
                    .accessibilityLabel("Nuevo tablero")
                    .accessibilityIdentifier("boards_add_button")
                }
            }
            .task { viewModel.refresh() }
            .sheet(isPresented: $showCreateSheet) {
                CreateBoardSheet { name, color in
                    viewModel.create(name: name, color: color)
                }
            }
            .confirmationDialog(
                "Eliminar tablero",
                isPresented: Binding(
                    get: { boardToDelete != nil },
                    set: { isPresented in
                        if !isPresented { boardToDelete = nil }
                    }
                ),
                titleVisibility: .visible,
                presenting: boardToDelete
            ) { board in
                Button("Eliminar", role: .destructive) {
                    viewModel.delete(id: board.id)
                }
            } message: { board in
                Text("Se eliminará el tablero «\(board.name)». Esta acción no se puede deshacer.")
            }
            .errorAlert(state.error, onDismiss: viewModel.clearError)
    }

    @ViewBuilder
    private func content(_ state: BoardListState) -> some View {
        if state.isLoading && state.boards.isEmpty {
            ProgressView()
        } else if state.boards.isEmpty {
            ContentUnavailableView(
                "Aún no tienes tableros",
                systemImage: "tray",
                description: Text("Crea el primero con el botón +")
            )
        } else {
            List(state.boards, id: \.id) { board in
                Button {
                    onBoardTap(board)
                } label: {
                    BoardRow(board: board)
                }
                .buttonStyle(.plain)
                .swipeActions {
                    Button(role: .destructive) {
                        boardToDelete = board
                    } label: {
                        Label("Eliminar", systemImage: "trash")
                    }
                }
            }
        }
    }
}

private struct BoardRow: View {

    let board: Board

    var body: some View {
        HStack(spacing: 12) {
            Circle()
                .fill(Color(hex: board.color))
                .frame(width: 16, height: 16)
            VStack(alignment: .leading, spacing: 2) {
                Text(board.name)
                    .font(.headline)
                Text(board.taskCount == 1 ? "1 tarea" : "\(board.taskCount) tareas")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .font(.footnote)
                .foregroundStyle(.tertiary)
        }
        .contentShape(Rectangle())
    }
}

private struct CreateBoardSheet: View {

    private static let colors = ["#FF9800", "#F44336", "#4CAF50", "#2196F3", "#9C27B0"]

    let onCreate: (String, String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var color = CreateBoardSheet.colors[0]

    var body: some View {
        NavigationStack {
            Form {
                Section("Nombre") {
                    TextField("Nombre", text: $name)
                        .accessibilityIdentifier("board_name_field")
                }
                Section("Color") {
                    HStack(spacing: 16) {
                        ForEach(Self.colors, id: \.self) { option in
                            Circle()
                                .fill(Color(hex: option))
                                .frame(width: 32, height: 32)
                                .overlay {
                                    if option == color {
                                        Circle().stroke(.primary, lineWidth: 3)
                                    }
                                }
                                .onTapGesture { color = option }
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .navigationTitle("Nuevo tablero")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Crear") {
                        onCreate(name, color)
                        dismiss()
                    }
                    .accessibilityIdentifier("board_create_button")
                }
            }
        }
        .presentationDetents([.medium])
    }
}