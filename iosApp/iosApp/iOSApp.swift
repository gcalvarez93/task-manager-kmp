// Path: iosApp/iosApp/iOSApp.swift
import FirebaseCore
import SharedLogic
import SwiftUI

@main
struct iOSApp: App {

    private let container: AppContainer

    init() {
        FirebaseApp.configure()
        let authRepository = FirebaseAuthRepository()
        // Los tests de interfaz parten siempre de "sin sesión" para ser reproducibles.
        if ProcessInfo.processInfo.arguments.contains("-ui-testing") {
            authRepository.signOut()
        }
        container = AppContainer(
            baseUrl: AppConfig.apiBaseURL,
            tokenProvider: FirebaseTokenProvider(),
            authRepository: authRepository
        )
    }

    var body: some Scene {
        WindowGroup {
            RootView(container: container)
        }
    }
}