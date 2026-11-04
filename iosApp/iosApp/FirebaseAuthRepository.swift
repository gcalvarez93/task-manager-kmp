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

    // Igual que `__signIn`: SKIE oculta `signUp`. Firebase deja la sesión iniciada con la cuenta nueva.
    func __signUp(email: String, password: String) async throws -> any AppResult {
        do {
            _ = try await Auth.auth().createUser(withEmail: email, password: password)
            return AppResultSuccess<AnyObject>(value: nil)
        } catch {
            logger.warning("Fallo al crear la cuenta: \(error.localizedDescription)")
            return AppResultFailure(error: mapSignUp(error))
        }
    }

    func signOut() {
        do {
            try Auth.auth().signOut()
        } catch {
            logger.error("Fallo al cerrar sesión: \(error.localizedDescription)")
        }
    }

    /// Errores de `signIn`: casi todo lo relativo a credenciales es `Unauthorized`.
    func map(_ error: Error) -> any AppError {
        guard let code = authErrorCode(of: error) else {
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

    /// Errores de `signUp`: aquí un email mal formado es un error de validación, no de credenciales.
    func mapSignUp(_ error: Error) -> any AppError {
        guard let code = authErrorCode(of: error) else {
            return AppErrorUnknown(message: error.localizedDescription)
        }
        switch code {
        case .emailAlreadyInUse:
            return AppErrorEmailAlreadyInUse.shared
        case .weakPassword:
            return AppErrorWeakPassword.shared
        case .invalidEmail:
            return AppErrorValidation(message: "El email no es válido")
        case .networkError:
            return AppErrorNetwork(message: error.localizedDescription)
        default:
            return AppErrorUnknown(message: error.localizedDescription)
        }
    }

    private func authErrorCode(of error: Error) -> AuthErrorCode? {
        let nsError = error as NSError
        guard nsError.domain == AuthErrorDomain else { return nil }
        return AuthErrorCode(rawValue: nsError.code)
    }
}