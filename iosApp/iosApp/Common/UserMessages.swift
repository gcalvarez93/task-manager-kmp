// Path: iosApp/iosApp/Common/UserMessages.swift
import SharedLogic
import SwiftUI

/// Texto para el usuario de un `AppError`. Los mensajes de validación ya vienen redactados.
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
    case is AppErrorEmailAlreadyInUse:
        return "Ya existe una cuenta con ese email"
    case is AppErrorWeakPassword:
        return "La contraseña es demasiado débil"
    default:
        return "Algo ha fallado. Inténtalo de nuevo"
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