// Path: iosApp/iosAppTests/UserMessagesTests.swift
import SharedLogic
import Testing
@testable import TaskManagerKMP

struct UserMessagesTests {

    @Test func validationErrorShowsItsOwnMessage() {
        let error = AppErrorValidation(message: "El nombre no puede estar vacio")

        #expect(userMessage(for: error) == "El nombre no puede estar vacio")
    }

    @Test func unauthorizedAsksToSignInAgain() {
        #expect(userMessage(for: AppErrorUnauthorized.shared) == "Tu sesión ha caducado. Cierra sesión y vuelve a entrar")
    }

    @Test func notFoundHasItsOwnMessage() {
        #expect(userMessage(for: AppErrorNotFound.shared) == "No se ha encontrado el elemento")
    }

    @Test func networkErrorMentionsTheConnection() {
        let error = AppErrorNetwork(message: "timeout")

        #expect(userMessage(for: error) == "Sin conexión. Comprueba tu red e inténtalo de nuevo")
    }

    @Test func timeoutAsksToTryAgain() {
        #expect(userMessage(for: AppErrorTimeout.shared) == "La operación ha tardado demasiado. Inténtalo de nuevo")
    }

    @Test func rejectedDataNeverShowsTheServerDetail() {
        let error = AppErrorRejected(statusCode: 400, detail: "{\"errors\":{\"Title\":[\"detalle interno\"]}}")

        let message = userMessage(for: error)

        #expect(message == "El servidor no ha aceptado los datos. Revísalos e inténtalo de nuevo")
        #expect(!message.contains("detalle interno"))
    }

    @Test func serverErrorNeverShowsTheServerMessage() {
        let error = AppErrorServer(statusCode: 500, message: "stacktrace interno")

        let message = userMessage(for: error)

        #expect(message == "El servidor no está disponible ahora. Inténtalo más tarde")
        #expect(!message.contains("stacktrace"))
    }

    @Test func emailAlreadyInUseHasItsOwnMessage() {
        #expect(userMessage(for: AppErrorEmailAlreadyInUse.shared) == "Ya existe una cuenta con ese email")
    }

    @Test func weakPasswordHasItsOwnMessage() {
        #expect(userMessage(for: AppErrorWeakPassword.shared) == "La contraseña es demasiado débil")
    }

    @Test func otherErrorsUseTheGenericMessage() {
        let invalid = AppErrorInvalidData(message: "fecha mal formada")
        let unknown = AppErrorUnknown(message: "???")

        #expect(userMessage(for: invalid) == "Algo ha fallado. Inténtalo de nuevo")
        #expect(userMessage(for: unknown) == "Algo ha fallado. Inténtalo de nuevo")
    }
}