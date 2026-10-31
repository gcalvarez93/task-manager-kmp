// Path: iosApp/iosApp/iOSApp.swift
import FirebaseCore
import SharedLogic
import SwiftUI

@main
struct iOSApp: App {

    private let container: AppContainer

    init() {
        FirebaseApp.configure()
        container = AppContainer(
            baseUrl: AppConfig.apiBaseURL,
            tokenProvider: FirebaseTokenProvider(),
            authRepository: FirebaseAuthRepository()
        )
    }

    var body: some Scene {
        WindowGroup {
            RootView(container: container)
        }
    }
}