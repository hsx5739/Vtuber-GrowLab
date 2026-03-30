import SwiftUI

struct SignInScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScreenContainer(title: "签到") {
                HStack(spacing: 12) {
                    MetricCapsule(metric: StatusMetric(title: "今日状态", value: model.state.signedToday ? "已签到" : "可领取", color: MCColor.green))
                    MetricCapsule(metric: StatusMetric(title: "连续天数", value: "\(model.state.streak)", color: MCColor.gold))
                }

                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    ForEach(model.state.signInDays) { day in
                        VStack(spacing: 8) {
                            Text(String(format: "%02d", day.day))
                                .font(.headline)
                                .foregroundStyle(MCColor.textPrimary)
                            Text(day.reward)
                                .font(.caption)
                                .foregroundStyle(MCColor.textSecondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background((day.day <= model.state.streak ? MCColor.gold : MCColor.panelSecondary).opacity(0.22), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
                    }
                }

                PrimaryButton(title: model.state.signedToday ? "今天已领取" : "立即签到", tint: MCColor.gold) {
                    model.signInToday()
                }
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("关闭") { dismiss() }
                        .foregroundStyle(MCColor.textSecondary)
                }
            }
        }
    }
}
