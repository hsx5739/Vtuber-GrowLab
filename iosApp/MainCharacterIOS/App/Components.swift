import SwiftUI

struct ScreenContainer<Content: View>: View {
    let title: String
    @ViewBuilder var content: Content

    var body: some View {
        ZStack {
            MCBackground()
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 18) {
                    Text(title)
                        .font(.system(size: 28, weight: .bold, design: .rounded))
                        .foregroundStyle(MCColor.textPrimary)
                    content
                }
                .padding(20)
                .padding(.bottom, 40)
            }
        }
    }
}

struct MetricCapsule: View {
    let metric: StatusMetric

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(metric.title)
                .font(.caption)
                .foregroundStyle(MCColor.textSecondary)
            Text(metric.value)
                .font(.system(size: 20, weight: .bold, design: .rounded))
                .foregroundStyle(MCColor.textPrimary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(metric.color.opacity(0.16), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(metric.color.opacity(0.28), lineWidth: 1)
        )
    }
}

struct PrimaryButton: View {
    let title: String
    let tint: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 15, weight: .semibold, design: .rounded))
                .foregroundStyle(.black)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(tint, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

struct OutlineButton: View {
    let title: String
    let tint: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 15, weight: .semibold, design: .rounded))
                .foregroundStyle(tint)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .stroke(tint.opacity(0.35), lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }
}

struct NoticeBanner: View {
    let text: String
    let dismiss: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "sparkles")
                .foregroundStyle(MCColor.gold)
            Text(text)
                .font(.system(size: 14, weight: .medium, design: .rounded))
                .foregroundStyle(MCColor.textPrimary)
            Spacer()
            Button("收起", action: dismiss)
                .font(.caption)
                .foregroundStyle(MCColor.textSecondary)
        }
        .padding(14)
        .mcPanel()
    }
}
