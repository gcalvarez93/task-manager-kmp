// Path: iosApp/iosApp/Common/UserMessages.swift
import SharedLogic
import SwiftUI

/// Texto para el usuario de un `AppError`. Los mensajes de validación ya vienen redactados por la app.
/// Lo que responde el servidor (`AppErrorRejected`, `AppErrorServer`) nunca se muestra: va a los logs.
func userMessage(for error: any AppError) -> String {
    switch error {
    case let validation as AppErrorValidation:
        return validation.message
    case is AppErrorUnauthorized:
        return "Tu sesión ha caducado. Cierra sesión y vuelve a entrar"
    case is AppErrorNotFound:
        return "No se ha encontrado el elemento"
    case is AppErrorNetwork:
        return "Sin conexión. Comprueba tu red e inténtalo de nuevo"
    case is AppErrorTimeout:
        return "La operación ha tardado demasiado. Inténtalo de nuevo"
    case is AppErrorRejected:
        return "El servidor no ha aceptado los datos. Revísalos e inténtalo de nuevo"
    case is AppErrorServer:
        return "El servidor no está disponible ahora. Inténtalo más tarde"
    case is AppErrorEmailAlreadyInUse:
        return "Ya existe una cuenta con ese email"
    case is AppErrorWeakPassword:
        return "La contraseña es demasiado débil"
    default:
        return "Algo ha fallado. Inténtalo de nuevo"
    }
}

/// La carga ha fallado y no hay nada que mostrar: se explica el motivo y se ofrece reintentar.
struct LoadErrorView: View {

    let title: String
    let error: any AppError
    let onRetry: () -> Void

    var body: some View {
        ContentUnavailableView {
            Label(title, systemImage: "exclamationmark.triangle")
        } description: {
            Text(userMessage(for: error))
        } actions: {
            Button("Reintentar", action: onRetry)
                .buttonStyle(.borderedProminent)
                .accessibilityIdentifier("load_retry_button")
        }
    }
}

/// Contenido centrado que ocupa la pantalla y se puede arrastrar para recargar.
/// Es un `ScrollView` a propósito: `.refreshable` solo funciona sobre contenido desplazable,
/// y así también se puede recargar cuando no hay lista (vacía o con error).
struct RefreshableMessage<Content: View>: View {

    let onRefresh: () async -> Void
    let content: Content

    init(onRefresh: @escaping () async -> Void, @ViewBuilder content: () -> Content) {
        self.onRefresh = onRefresh
        self.content = content()
    }

    var body: some View {
        ScrollView {
            content.containerRelativeFrame(.vertical)
        }
        .refreshable { await onRefresh() }
    }
}

extension View {
    /// Muestra [error] en una alerta y avisa con [onDismiss] cuando el usuario la cierra,
    /// para que el store lo limpie.
    func errorAlert(_ error: (any AppError)?, onDismiss: @escaping () -> Void) -> some View {
        alert(
            "Algo ha fallado",
            isPresented: Binding(
                get: { error != nil },
                set: { isPresented in
                    if !isPresented { onDismiss() }
                }
            )
        ) {
            Button("Aceptar", role: .cancel) {}
        } message: {
            Text(error.map { userMessage(for: $0) } ?? "")
        }
    }
}