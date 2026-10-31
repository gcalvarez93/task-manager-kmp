// Path: iosApp/iosApp/FirebaseTokenProvider.swift
import FirebaseAuth
import SharedLogic

/// Entrega al módulo compartido el ID token de Firebase del usuario actual.
/// Si no hay sesión devuelve `nil` y la petición se envía sin autorización.
final class FirebaseTokenProvider: NSObject, TokenProvider, @unchecked Sendable {

    // SKIE oculta `idToken` (suspend en Kotlin) como `__idToken` para quien implementa el protocolo.
    func __idToken() async throws -> String? {
        guard let user = Auth.auth().currentUser else { return nil }
        return try await user.getIDToken()
    }
}