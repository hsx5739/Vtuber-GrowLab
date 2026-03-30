import SwiftUI

struct HomeScreen: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        ScreenContainer(title: "首页") {
            if let notice = model.state.notice {
                NoticeBanner(text: notice) {
                    model.clearNotice()
                }
            }

            VStack(alignment: .leading, spacing: 16) {
                Text("晚上好，\(model.state.nickname)")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(MCColor.textPrimary)
                Text("今天也继续和 \(model.state.companionName) 把状态一点点养起来。")
                    .foregroundStyle(MCColor.textSecondary)
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    ForEach(model.homeMetrics) { metric in
                        MetricCapsule(metric: metric)
                    }
                }
            }
            .padding(18)
            .mcPanel()

            VStack(alignment: .leading, spacing: 14) {
                Text("陪伴舞台")
                    .font(.headline)
                    .foregroundStyle(MCColor.textPrimary)
                RoundedRectangle(cornerRadius: 28, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [MCColor.panelSecondary, MCColor.panel, MCColor.background],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(height: 260)
                    .overlay(alignment: .bottomLeading) {
                        VStack(alignment: .leading, spacing: 10) {
                            Text(model.state.companionName)
                                .font(.system(size: 30, weight: .bold, design: .rounded))
                                .foregroundStyle(MCColor.textPrimary)
                            Text("“今天不需要一下子做很多，把第一步落下来就够了。”")
                                .foregroundStyle(MCColor.textSecondary)
                        }
                        .padding(22)
                    }
            }
            .padding(18)
            .mcPanel()

            HStack(spacing: 12) {
                PrimaryButton(title: "签到", tint: MCColor.gold) {
                    model.activeSheet = .signIn
                }
                PrimaryButton(title: "抽卡", tint: MCColor.pink) {
                    model.activeSheet = .gacha
                }
                PrimaryButton(title: "商店", tint: MCColor.blue) {
                    model.activeSheet = .shop
                }
            }

            VStack(alignment: .leading, spacing: 12) {
                Text("今日摘要")
                    .font(.headline)
                    .foregroundStyle(MCColor.textPrimary)
                ForEach(model.state.dailyTasks.prefix(3)) { task in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(task.title)
                                .foregroundStyle(MCColor.textPrimary)
                            Spacer()
                            Text(task.completed ? "已完成" : "\(task.progress)/\(task.target)")
                                .foregroundStyle(task.tint)
                        }
                        Text(task.summary)
                            .font(.subheadline)
                            .foregroundStyle(MCColor.textSecondary)
                    }
                    .padding(14)
                    .background(task.tint.opacity(0.1), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
                }
            }
            .padding(18)
            .mcPanel()
        }
    }
}
