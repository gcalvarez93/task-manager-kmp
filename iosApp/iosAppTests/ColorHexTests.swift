// Path: iosApp/iosAppTests/ColorHexTests.swift
import SwiftUI
import Testing
import UIKit
@testable import TaskManagerKMP

struct ColorHexTests {

    private func components(of color: Color) -> (r: Double, g: Double, b: Double) {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        UIColor(color).getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        return (Double(red), Double(green), Double(blue))
    }

    @Test func parsesAHexColorWithHash() {
        let (r, g, b) = components(of: Color(hex: "#FF9800"))

        #expect(abs(r - 1.0) < 0.01)
        #expect(abs(g - 152.0 / 255.0) < 0.01)
        #expect(abs(b - 0.0) < 0.01)
    }

    @Test func parsesAHexColorWithoutHash() {
        let (r, g, b) = components(of: Color(hex: "2196F3"))

        #expect(abs(r - 33.0 / 255.0) < 0.01)
        #expect(abs(g - 150.0 / 255.0) < 0.01)
        #expect(abs(b - 243.0 / 255.0) < 0.01)
    }

    @Test func invalidValuesFallBackToGray() {
        #expect(Color(hex: "no-es-un-color") == Color.gray)
        #expect(Color(hex: "#FFF") == Color.gray)
        #expect(Color(hex: "") == Color.gray)
    }
}