// Path: iosApp/iosApp/iOSApp.swift
import FirebaseCore
import Logging
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
            authRepository: authRepository,
            logger: SwiftLogErrorLogger()
        )
    }

    var body: some Scene {
        WindowGroup {
            RootView(container: container)
        }
    }
}

/// Lleva los errores del módulo compartido a swift-log. El detalle técnico va aquí y nunca a la pantalla.
/// Está en este archivo para no tener que añadir uno nuevo al target de Xcode.
final class SwiftLogErrorLogger: NSObject, ErrorLogger, @unchecked Sendable {

    private let logger = Logger(label: "com.castrodev.taskmanagerkmp.shared")

    func warn(message: String) {
        logger.warning("\(message)")
    }
}