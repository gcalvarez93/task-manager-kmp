// Path: iosApp/iosAppTests/LoginViewModelTests.swift
import SharedLogic
import Testing
@testable import TaskManagerKMP

@MainActor
struct LoginViewModelTests {

    private func makeViewModel(auth: FakeAuthRepository) -> LoginViewModel {
        let container = AppContainer(
            baseUrl: "https://api.test/",
            tokenProvider: FakeTokenProvider(),
            authRepository: auth
        )
        return LoginViewModel(container: container)
    }

    @Test func startsSignedOutWithoutErrors() {
        let viewModel = makeViewModel(auth: FakeAuthRepository(signedIn: false))

        #expect(viewModel.state.isSignedIn == false)
        #expect(viewModel.state.emailError == nil)
        #expect(viewModel.state.passwordError == nil)
    }

    @Test func startsSignedInWhenThereIsAnExistingSession() {
        let viewModel = makeViewModel(auth: FakeAuthRepository(signedIn: true))

        #expect(viewModel.state.isSignedIn == true)
    }

    @Test func submittingEmptyFieldsShowsValidationErrorsAndDoesNotCallTheRepository() async {
        let auth = FakeAuthRepository()
        let viewModel = makeViewModel(auth: auth)

        viewModel.submit()
        let reflected = await waitUntil { viewModel.state.emailError != nil }

        #expect(reflected)
        #expect(viewModel.state.emailError == .empty)
        #expect(viewModel.state.passwordError == .empty)
        #expect(auth.calls.isEmpty)
    }

    @Test func submittingAnInvalidEmailShowsTheInvalidEmailError() async {
        let auth = FakeAuthRepository()
        let viewModel = makeViewModel(auth: auth)

        viewModel.onEmailChange("esto-no-es-un-email")
        viewModel.onPasswordChange("secreto1")
        viewModel.submit()
        let reflected = await waitUntil { viewModel.state.emailError != nil }

        #expect(reflected)
        #expect(viewModel.state.emailError == .invalid)
        #expect(auth.calls.isEmpty)
    }

    @Test func aSuccessfulSignInUpdatesTheStateAndSendsTheTrimmedEmail() async {
        let auth = FakeAuthRepository()
        let viewModel = makeViewModel(auth: auth)

        viewModel.onEmailChange("  usuario@test.com ")
        viewModel.onPasswordChange("secreto1")
        viewModel.submit()
        let signedIn = await waitUntil { viewModel.state.isSignedIn }

        #expect(signedIn)
        #expect(auth.calls == [SignInCall(email: "usuario@test.com", password: "secreto1")])
    }

    @Test func wrongCredentialsExposeAnUnauthorizedError() async {
        let auth = FakeAuthRepository()
        auth.result = AppResultFailure(error: AppErrorUnauthorized.shared)
        let viewModel = makeViewModel(auth: auth)

        viewModel.onEmailChange("usuario@test.com")
        viewModel.onPasswordChange("incorrecta")
        viewModel.submit()
        let failed = await waitUntil { viewModel.state.error != nil }

        #expect(failed)
        #expect(viewModel.state.error is AppErrorUnauthorized)
        #expect(viewModel.state.isSignedIn == false)
    }
}