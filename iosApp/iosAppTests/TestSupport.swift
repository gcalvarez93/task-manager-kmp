// Path: iosApp/iosAppTests/TestSupport.swift
import Foundation
import SharedLogic

/// Llamada registrada por [FakeAuthRepository] (email y contraseña recibidos).
struct SignInCall: Equatable {
    let email: String
    let password: String
}

/// `AuthRepository` falso: no usa Firebase. Devuelve el [result] configurado y anota las llamadas.
final class FakeAuthRepository: NSObject, AuthRepository, @unchecked Sendable {

    private let lock = NSLock()
    private var signedIn: Bool
    private var recordedCalls: [SignInCall] = []
    private var recordedSignUpCalls: [SignInCall] = []

    /// Resultado que devolverán `signIn` y `signUp`. Se configura antes de usar el repositorio.
    var result: any AppResult = AppResultSuccess<AnyObject>(value: nil)

    init(signedIn: Bool = false) {
        self.signedIn = signedIn
        super.init()
    }

    var isSignedIn: Bool {
        lock.withLock { signedIn }
    }

    /// Llamadas a `signIn`.
    var calls: [SignInCall] {
        lock.withLock { recordedCalls }
    }

    /// Llamadas a `signUp`.
    var signUpCalls: [SignInCall] {
        lock.withLock { recordedSignUpCalls }
    }

    func __signIn(email: String, password: String) async throws -> any AppResult {
        lock.withLock { recordedCalls.append(SignInCall(email: email, password: password)) }
        return result
    }

    func __signUp(email: String, password: String) async throws -> any AppResult {
        lock.withLock { recordedSignUpCalls.append(SignInCall(email: email, password: password)) }
        return result
    }

    func signOut() {
        lock.withLock { signedIn = false }
    }
}

/// `TokenProvider` falso: devuelve siempre el mismo token.
final class FakeTokenProvider: NSObject, TokenProvider, @unchecked Sendable {

    func __idToken() async throws -> String? {
        "token-de-prueba"
    }
}

/// Espera, sin bloquear, a que se cumpla [condition]. Devuelve `false` si se agota el tiempo.
@MainActor
func waitUntil(timeout: Duration = .seconds(3), _ condition: () -> Bool) async -> Bool {
    let deadline = ContinuousClock.now + timeout
    while ContinuousClock.now < deadline {
        if condition() { return true }
        try? await Task.sleep(for: .milliseconds(20))
    }
    return condition()
}