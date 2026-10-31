// Path: iosApp/iosApp/Login/LoginView.swift
import SharedLogic
import SwiftUI

/// Conecta el `LoginViewModel` con la pantalla y avisa cuando se ha iniciado sesión.
struct LoginView: View {

    let onSignedIn: () -> Void
    @State private var viewModel: LoginViewModel

    init(container: AppContainer, onSignedIn: @escaping () -> Void) {
        self.onSignedIn = onSignedIn
        _viewModel = State(initialValue: LoginViewModel(container: container))
    }

    var body: some View {
        let state = viewModel.state
        LoginContent(
            state: state,
            onEmailChange: viewModel.onEmailChange,
            onPasswordChange: viewModel.onPasswordChange,
            onSubmit: viewModel.submit
        )
        .onChange(of: state.isSignedIn) { _, isSignedIn in
            if isSignedIn { onSignedIn() }
        }
    }
}

/// Pantalla sin lógica: solo pinta el estado y devuelve los eventos del usuario.
private struct LoginContent: View {

    let state: LoginState
    let onEmailChange: (String) -> Void
    let onPasswordChange: (String) -> Void
    let onSubmit: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Iniciar sesión")
                .font(.largeTitle.bold())

            field(
                title: "Email",
                errorText: emailErrorText,
                content: {
                    TextField("Email", text: Binding(get: { state.email }, set: onEmailChange))
                        .keyboardType(.emailAddress)
                        .textContentType(.emailAddress)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                }
            )

            field(
                title: "Contraseña",
                errorText: passwordErrorText,
                content: {
                    SecureField("Contraseña", text: Binding(get: { state.password }, set: onPasswordChange))
                        .textContentType(.password)
                }
            )

            if let message = generalErrorText {
                Text(message)
                    .font(.footnote)
                    .foregroundStyle(.red)
            }

            Button(action: onSubmit) {
                if state.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Entrar")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(state.isLoading)
        }
        .padding(24)
        .frame(maxHeight: .infinity, alignment: .center)
    }

    private func field<Content: View>(
        title: String,
        errorText: String?,
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            content()
                .textFieldStyle(.roundedBorder)
                .accessibilityLabel(title)
            if let errorText {
                Text(errorText)
                    .font(.footnote)
                    .foregroundStyle(.red)
            }
        }
    }

    private var emailErrorText: String? {
        guard let error = state.emailError else { return nil }
        switch error {
        case .empty: return "Escribe tu email"
        case .invalid: return "El email no es válido"
        }
    }

    private var passwordErrorText: String? {
        guard state.passwordError != nil else { return nil }
        return "Escribe tu contraseña"
    }

    private var generalErrorText: String? {
        guard let error = state.error else { return nil }
        switch error {
        case is AppErrorUnauthorized: return "Email o contraseña incorrectos"
        case is AppErrorNetwork: return "Sin conexión. Comprueba tu red e inténtalo de nuevo"
        default: return "No se ha podido iniciar sesión. Inténtalo de nuevo"
        }
    }
}