// Path: iosApp/iosAppTests/FirebaseAuthRepositoryTests.swift
import Foundation
import SharedLogic
import Testing
@testable import TaskManagerKMP

/// Prueba solo el traspaso de errores de Firebase a `AppError`; no usa la red ni el SDK real.
struct FirebaseAuthRepositoryTests {

    // Dominio y códigos documentados de Firebase Auth (AuthErrorDomain / AuthErrorCode).
    private let domain = "FIRAuthErrorDomain"
    private let invalidCredential = 17004
    private let emailAlreadyInUse = 17007
    private let invalidEmail = 17008
    private let wrongPassword = 17009
    private let tooManyRequests = 17010
    private let userNotFound = 17011
    private let networkError = 17020
    private let weakPassword = 17026

    private func firebaseError(code: Int) -> NSError {
        NSError(domain: domain, code: code, userInfo: [NSLocalizedDescriptionKey: "error de prueba"])
    }

    // MARK: - signIn

    @Test func credentialErrorsMapToUnauthorized() {
        let repository = FirebaseAuthRepository()

        for code in [invalidCredential, invalidEmail, wrongPassword, userNotFound] {
            #expect(repository.map(firebaseError(code: code)) is AppErrorUnauthorized)
        }
    }

    @Test func networkErrorMapsToNetwork() {
        let repository = FirebaseAuthRepository()

        #expect(repository.map(firebaseError(code: networkError)) is AppErrorNetwork)
    }

    @Test func otherFirebaseErrorsMapToUnknown() {
        let repository = FirebaseAuthRepository()

        #expect(repository.map(firebaseError(code: tooManyRequests)) is AppErrorUnknown)
    }

    @Test func errorsFromAnotherDomainMapToUnknown() {
        let repository = FirebaseAuthRepository()
        let foreign = NSError(domain: "OtroDominio", code: wrongPassword)

        #expect(repository.map(foreign) is AppErrorUnknown)
    }

    // MARK: - signUp

    @Test func signUpEmailAlreadyInUseMapsToEmailAlreadyInUse() {
        let repository = FirebaseAuthRepository()

        #expect(repository.mapSignUp(firebaseError(code: emailAlreadyInUse)) is AppErrorEmailAlreadyInUse)
    }

    @Test func signUpWeakPasswordMapsToWeakPassword() {
        let repository = FirebaseAuthRepository()

        #expect(repository.mapSignUp(firebaseError(code: weakPassword)) is AppErrorWeakPassword)
    }

    @Test func signUpInvalidEmailMapsToValidationInsteadOfUnauthorized() {
        let repository = FirebaseAuthRepository()

        #expect(repository.mapSignUp(firebaseError(code: invalidEmail)) is AppErrorValidation)
    }

    @Test func signUpNetworkErrorMapsToNetwork() {
        let repository = FirebaseAuthRepository()

        #expect(repository.mapSignUp(firebaseError(code: networkError)) is AppErrorNetwork)
    }

    @Test func signUpErrorsFromAnotherDomainOrUnlistedMapToUnknown() {
        let repository = FirebaseAuthRepository()
        let foreign = NSError(domain: "OtroDominio", code: emailAlreadyInUse)

        #expect(repository.mapSignUp(foreign) is AppErrorUnknown)
        #expect(repository.mapSignUp(firebaseError(code: tooManyRequests)) is AppErrorUnknown)
    }
}