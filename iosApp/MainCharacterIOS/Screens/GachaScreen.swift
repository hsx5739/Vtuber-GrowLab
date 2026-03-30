import SwiftUI

struct GachaScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScreenContainer(title: "抽卡") {
                VStack(alignment: .leading, spacing: 14) {
                    Text("常驻星愿池")
                        .font(.title3.weight(.bold))
                        .foregroundStyle(MCColor.textPrimary)
                    Text("保留 Android 端的资源循环结构：任务 / 商店 / 抽卡彼此联动。")
                        .foregroundStyle(MCColor.textSecondary)
                    HStack(spacing: 12) {
                        MetricCapsule(metric: StatusMetric(title: "抽奖券", value: "\(model.state.tickets)", color: MCColor.lavender))
                        MetricCapsule(metric: StatusMetric(title: "当前星尘", value: "\(model.state.stardust)", color: MCColor.gold))
                    }
                    PrimaryButton(title: "单抽", tint: MCColor.pink) {
                        model.draw()
                    }
                }
                .padding(18)
                .mcPanel()

                if let reward = model.state.latestGachaResults.first {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("本次结果")
                            .font(.headline)
                            .foregroundStyle(MCColor.textPrimary)
                        Text(reward.title)
                            .font(.title3.weight(.bold))
                            .foregroundStyle(reward.tint)
                        Text(reward.detail)
                            .foregroundStyle(MCColor.textSecondary)
                        Text(reward.rarity)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(reward.tint)
                    }
                    .padding(18)
                    .mcPanel()
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
