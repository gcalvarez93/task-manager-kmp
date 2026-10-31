// Path: iosApp/iosApp/FirebaseAuthRepository.swift
import FirebaseAuth
import Logging
import SharedLogic

/// Implementación de `AuthRepository` con Firebase Auth. Los errores no se lanzan:
/// se devuelven dentro de `AppResultFailure`, igual que en Android.
final class FirebaseAuthRepository: NSObject, AuthRepository, @unchecked Sendable {

    private let logger = Logger(label: "com.castrodev.taskmanagerkmp.auth")

    var isSignedIn: Bool {
        Auth.auth().currentUser != nil
    }

    // SKIE oculta `signIn` (suspend en Kotlin) como `__signIn` para quien implementa el protocolo.
    func __signIn(email: String, password: String) async throws -> any AppResult {
        do {
            _ = try await Auth.auth().signIn(withEmail: email, password: password)
            return AppResultSuccess<AnyObject>(value: nil)
        } catch {
            logger.warning("Fallo al iniciar sesión: \(error.localizedDescription)")
            return AppResultFailure(error: map(error))
        }
    }

    func signOut() {
        do {
            try Auth.auth().signOut()
        } catch {
            logger.error("Fallo al cerrar sesión: \(error.localizedDescription)")
        }
    }

    private func map(_ error: Error) -> any AppError {
        let nsError = error as NSError
        guard nsError.domain == AuthErrorDomain,
              let code = AuthErrorCode(rawValue: nsError.code) else {
            return AppErrorUnknown(message: error.localizedDescription)
        }
        switch code {
        case .invalidCredential, .wrongPassword, .userNotFound, .invalidEmail:
            return AppErrorUnauthorized.shared
        case .networkError:
            return AppErrorNetwork(message: error.localizedDescription)
        default:
            return AppErrorUnknown(message: error.localizedDescription)
        }
    }
}