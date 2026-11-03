// Path: iosApp/iosApp/Common/Color+Hex.swift
import SwiftUI

extension Color {
    /// Crea un color desde `#RRGGBB`. Si la API devuelve algo raro usa gris en vez de romper la pantalla.
    init(hex: String) {
        let digits = hex.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
        guard digits.count == 6, let value = UInt32(digits, radix: 16) else {
            self = .gray
            return
        }
        self.init(
            red: Double((value >> 16) & 0xFF) / 255,
            green: Double((value >> 8) & 0xFF) / 255,
            blue: Double(value & 0xFF) / 255
        )
    }
}