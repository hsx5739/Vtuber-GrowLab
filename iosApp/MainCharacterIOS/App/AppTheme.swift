import SwiftUI

enum MCColor {
    static let background = Color(red: 10 / 255, green: 15 / 255, blue: 31 / 255)
    static let panel = Color(red: 20 / 255, green: 28 / 255, blue: 54 / 255)
    static let panelSecondary = Color(red: 28 / 255, green: 37 / 255, blue: 68 / 255)
    static let line = Color.white.opacity(0.08)
    static let textPrimary = Color.white
    static let textSecondary = Color(red: 186 / 255, green: 197 / 255, blue: 227 / 255)
    static let pink = Color(red: 247 / 255, green: 182 / 255, blue: 209 / 255)
    static let blue = Color(red: 158 / 255, green: 216 / 255, blue: 255 / 255)
    static let green = Color(red: 200 / 255, green: 255 / 255, blue: 155 / 255)
    static let gold = Color(red: 255 / 255, green: 227 / 255, blue: 122 / 255)
    static let lavender = Color(red: 214 / 255, green: 199 / 255, blue: 255 / 255)
}

struct MCBackground: View {
    var body: some View {
        LinearGradient(
            colors: [
                MCColor.background,
                Color(red: 18 / 255, green: 22 / 255, blue: 52 / 255),
                Color(red: 7 / 255, green: 10 / 255, blue: 25 / 255)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        .overlay(alignment: .topTrailing) {
            Circle()
                .fill(MCColor.pink.opacity(0.18))
                .frame(width: 220, height: 220)
                .blur(radius: 20)
                .offset(x: 70, y: -60)
        }
        .ignoresSafeArea()
    }
}

struct MCPanelModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: 24, style: .continuous)
                    .fill(MCColor.panel.opacity(0.92))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 24, style: .continuous)
                    .stroke(MCColor.line, lineWidth: 1)
            )
    }
}

extension View {
    func mcPanel() -> some View {
        modifier(MCPanelModifier())
    }
}
