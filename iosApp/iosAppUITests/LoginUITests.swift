// Path: iosApp/iosAppUITests/LoginUITests.swift
import XCTest

/// Flujo de validación del login. No usa la red: los errores salen antes de llamar a Firebase.
final class LoginUITests: XCTestCase {

    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        // Con este argumento la app arranca siempre sin sesión.
        app.launchArguments.append("-ui-testing")
        app.launch()
    }

    func testLoginScreenIsShownWhenThereIsNoSession() {
        XCTAssertTrue(app.staticTexts["Iniciar sesión"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.textFields["Email"].exists)
        XCTAssertTrue(app.secureTextFields["Contraseña"].exists)
        XCTAssertTrue(app.buttons["Entrar"].exists)
    }

    func testSubmittingEmptyFieldsShowsBothValidationErrors() {
        XCTAssertTrue(app.buttons["Entrar"].waitForExistence(timeout: 5))

        app.buttons["Entrar"].tap()

        XCTAssertTrue(app.staticTexts["Escribe tu email"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Escribe tu contraseña"].exists)
    }

    func testAnInvalidEmailShowsTheInvalidEmailError() {
        let email = app.textFields["Email"]
        XCTAssertTrue(email.waitForExistence(timeout: 5))
        email.tap()
        email.typeText("esto-no-es-un-email")

        let password = app.secureTextFields["Contraseña"]
        password.tap()
        password.typeText("secreto1")

        app.buttons["Entrar"].tap()

        XCTAssertTrue(app.staticTexts["El email no es válido"].waitForExistence(timeout: 3))
    }
}