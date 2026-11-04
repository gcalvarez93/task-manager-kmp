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
            onToggleMode: viewModel.toggleMode,
            onSubmit: viewModel.submit
        )
        .onChange(of: state.isSignedIn) { _, isSignedIn in
            if isSignedIn { onSignedIn() }
        }
    }
}

/// Pantalla sin lógica: solo pinta el estado y devuelve los eventos del usuario.
/// Sirve para iniciar sesión y para registrarse según `state.mode`.
private struct LoginContent: View {

    let state: LoginState
    let onEmailChange: (String) -> Void
    let onPasswordChange: (String) -> Void
    let onToggleMode: () -> Void
    let onSubmit: () -> Void

    @State private var isPasswordVisible = false
    @FocusState private var focusedField: Field?

    private enum Field { case email, password }

    // Mismos colores de marca que la pantalla de Android (Material 3 por defecto).
    private static let brandStart = Color(hex: "#6650A4")
    private static let brandEnd = Color(hex: "#7D5260")
    private static let brandGradient = LinearGradient(
        colors: [brandStart, brandEnd],
        startPoint: .top,
        endPoint: .bottom
    )

    private var isSignUp: Bool { state.mode == .signUp }

    /// Longitud mínima de contraseña definida en el módulo compartido.
    private var minPasswordLength: Int { Int(LoginValidationKt.MIN_PASSWORD_LENGTH) }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                header

                form
                    .padding(.horizontal, 24)
                    .padding(.top, -56)

                modeSwitch
                    .padding(.top, 16)
                    .padding(.bottom, 24)
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .background(Color(.systemGroupedBackground).ignoresSafeArea())
        .animation(.easeInOut(duration: 0.2), value: state.mode)
    }

    // MARK: - Cabecera

    private var header: some View {
        VStack(spacing: 12) {
            ZStack {
                Circle().fill(.white.opacity(0.18))
                Image(systemName: "checkmark")
                    .font(.system(size: 30, weight: .bold))
                    .foregroundStyle(.white)
            }
            .frame(width: 72, height: 72)
            .accessibilityHidden(true)

            Text(isSignUp ? "Crear cuenta" : "Iniciar sesión")
                .font(.largeTitle.bold())
                .foregroundStyle(.white)

            Text(isSignUp ? "Empieza a organizar tus tareas" : "Organiza tus tableros y tareas")
                .font(.subheadline)
                .foregroundStyle(.white.opacity(0.85))
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 24)
        .padding(.bottom, 80)
        .background {
            // `ignoresSafeArea` extiende el degradado bajo la barra de estado.
            UnevenRoundedRectangle(bottomLeadingRadius: 36, bottomTrailingRadius: 36)
                .fill(Self.brandGradient)
                .ignoresSafeArea(edges: .top)
        }
    }

    // MARK: - Formulario

    private var form: some View {
        VStack(alignment: .leading, spacing: 16) {
            emailField
            passwordField

            if let message = generalErrorText {
                Text(message)
                    .font(.footnote)
                    .foregroundStyle(.red)
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.red.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
            }

            submitButton
        }
        .padding(24)
        .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 24))
        .shadow(color: .black.opacity(0.08), radius: 12, y: 4)
    }

    private var emailField: some View {
        VStack(alignment: .leading, spacing: 4) {
            inputRow(icon: "envelope", hasError: emailErrorText != nil) {
                TextField("Email", text: emailBinding)
                    .keyboardType(.emailAddress)
                    .textContentType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .focused($focusedField, equals: .email)
                    .submitLabel(.next)
                    .onSubmit { focusedField = .password }
                    .accessibilityLabel("Email")
            }
            if let errorText = emailErrorText {
                errorLabel(errorText)
            }
        }
    }

    private var passwordField: some View {
        VStack(alignment: .leading, spacing: 4) {
            inputRow(icon: "lock", hasError: passwordErrorText != nil) {
                Group {
                    if isPasswordVisible {
                        TextField("Contraseña", text: passwordBinding)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                    } else {
                        SecureField("Contraseña", text: passwordBinding)
                    }
                }
                .textContentType(.password)
                .focused($focusedField, equals: .password)
                .submitLabel(.go)
                .onSubmit(submit)
                .accessibilityLabel("Contraseña")

                Button {
                    isPasswordVisible.toggle()
                } label: {
                    Image(systemName: isPasswordVisible ? "eye.slash" : "eye")
                        .foregroundStyle(.secondary)
                }
                .accessibilityLabel(isPasswordVisible ? "Ocultar contraseña" : "Mostrar contraseña")
            }

            if let errorText = passwordErrorText {
                errorLabel(errorText)
            } else if isSignUp {
                Text("Mínimo \(minPasswordLength) caracteres")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var submitButton: some View {
        Button(action: submit) {
            Group {
                if state.isLoading {
                    ProgressView().tint(.white)
                } else {
                    Text(isSignUp ? "Crear cuenta" : "Entrar")
                        .font(.headline)
                }
            }
            .frame(maxWidth: .infinity, minHeight: 28)
        }
        .buttonStyle(.borderedProminent)
        .controlSize(.large)
        .tint(Self.brandStart)
        .disabled(state.isLoading)
    }

    /// Enlace inferior para alternar entre iniciar sesión y crear cuenta.
    private var modeSwitch: some View {
        HStack(spacing: 4) {
            Text(isSignUp ? "¿Ya tienes cuenta?" : "¿No tienes cuenta?")
                .foregroundStyle(.secondary)
            Button(isSignUp ? "Inicia sesión" : "Regístrate", action: onToggleMode)
                .fontWeight(.semibold)
                .tint(Self.brandStart)
                .disabled(state.isLoading)
        }
        .font(.subheadline)
    }

    // MARK: - Piezas

    private func inputRow<Content: View>(
        icon: String,
        hasError: Bool,
        @ViewBuilder content: () -> Content
    ) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .foregroundStyle(.secondary)
                .frame(width: 20)
                .accessibilityHidden(true)
            content()
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 14)
        .background(Color(.systemGray6), in: RoundedRectangle(cornerRadius: 14))
        .overlay {
            RoundedRectangle(cornerRadius: 14)
                .stroke(hasError ? Color.red : Color.clear, lineWidth: 1)
        }
    }

    private func errorLabel(_ text: String) -> some View {
        Text(text)
            .font(.footnote)
            .foregroundStyle(.red)
    }

    // Los bindings ignoran escrituras sin cambios: SwiftUI puede reenviar el mismo texto al perder
    // el foco, y el store interpreta cualquier edición como "el usuario corrige el campo" y borraría
    // el error de validación que acaba de mostrar.
    private var emailBinding: Binding<String> {
        Binding(
            get: { state.email },
            set: { newValue in
                if newValue != state.email { onEmailChange(newValue) }
            }
        )
    }

    private var passwordBinding: Binding<String> {
        Binding(
            get: { state.password },
            set: { newValue in
                if newValue != state.password { onPasswordChange(newValue) }
            }
        )
    }

    private func submit() {
        focusedField = nil
        onSubmit()
    }

    // MARK: - Textos

    private var emailErrorText: String? {
        guard let error = state.emailError else { return nil }
        switch error {
        case .empty: return "Escribe tu email"
        case .invalid: return "El email no es válido"
        }
    }

    private var passwordErrorText: String? {
        guard let error = state.passwordError else { return nil }
        switch error {
        case .empty: return "Escribe tu contraseña"
        case .tooShort: return "La contraseña debe tener al menos \(minPasswordLength) caracteres"
        }
    }

    private var generalErrorText: String? {
        guard let error = state.error else { return nil }
        switch error {
        case is AppErrorUnauthorized: return "Email o contraseña incorrectos"
        case is AppErrorNetwork: return "Sin conexión. Comprueba tu red e inténtalo de nuevo"
        case is AppErrorEmailAlreadyInUse: return "Ya existe una cuenta con ese email"
        case is AppErrorWeakPassword: return "La contraseña es demasiado débil"
        case let validation as AppErrorValidation: return validation.message
        default:
            return isSignUp
                ? "No se ha podido crear la cuenta. Inténtalo de nuevo"
                : "No se ha podido iniciar sesión. Inténtalo de nuevo"
        }
    }
}