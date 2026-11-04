// Path: iosApp/iosApp/Login/LoginViewModel.swift
import Observation
import SharedLogic

/// ViewModel fino: la lógica vive en el `LoginStore` compartido. Aquí solo se
/// refleja su estado para SwiftUI y se cancela el trabajo cuando la pantalla desaparece.
@MainActor
@Observable
final class LoginViewModel {

    private(set) var state: LoginState

    @ObservationIgnored private let store: LoginStore

    // `nonisolated(unsafe)`: solo se tocan en `init` y en `deinit`, que no está aislado al hilo principal.
    @ObservationIgnored private nonisolated(unsafe) let scope = ScreenScope()
    @ObservationIgnored private nonisolated(unsafe) var observation: Task<Void, Never>?

    init(container: AppContainer) {
        store = container.createLoginStore(scope: scope)
        state = store.state.value
        observation = Task { [weak self, store] in
            for await newState in store.state {
                self?.state = newState
            }
        }
    }

    deinit {
        observation?.cancel()
        scope.close()
    }

    func onEmailChange(_ email: String) {
        store.onEmailChange(email: email)
    }

    func onPasswordChange(_ password: String) {
        store.onPasswordChange(password: password)
    }

    /// Alterna entre iniciar sesión y crear cuenta.
    func toggleMode() {
        store.toggleMode()
    }

    func submit() {
        store.submit()
    }
}