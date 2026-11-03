// Path: iosApp/iosApp/RootView.swift
import SharedLogic
import SwiftUI

/// Decide qué se ve según haya sesión o no. Arranca en los tableros si ya hay sesión.
struct RootView: View {

    let container: AppContainer
    @State private var isSignedIn: Bool

    init(container: AppContainer) {
        self.container = container
        _isSignedIn = State(initialValue: container.authRepository.isSignedIn)
    }

    var body: some View {
        if isSignedIn {
            MainFlowView(container: container) {
                container.authRepository.signOut()
                isSignedIn = false
            }
        } else {
            LoginView(container: container) {
                isSignedIn = true
            }
        }
    }
}