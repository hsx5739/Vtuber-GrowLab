import SwiftUI

struct ProfileScreen: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        ScreenContainer(title: "我的") {
            VStack(alignment: .leading, spacing: 16) {
                HStack(spacing: 16) {
                    Circle()
                        .fill(MCColor.pink.opacity(0.24))
                        .frame(width: 72, height: 72)
                        .overlay {
                            Image(systemName: "person.fill")
                                .font(.title)
                                .foregroundStyle(MCColor.pink)
                        }
                    VStack(alignment: .leading, spacing: 6) {
                        Text(model.state.nickname)
                            .font(.title3.weight(.bold))
                            .foregroundStyle(MCColor.textPrimary)
                        Text("陪伴对象：\(model.state.companionName)")
                            .foregroundStyle(MCColor.textSecondary)
                    }
                }

                HStack(spacing: 12) {
                    MetricCapsule(metric: StatusMetric(title: "星尘", value: "\(model.state.stardust)", color: MCColor.gold))
                    MetricCapsule(metric: StatusMetric(title: "抽奖券", value: "\(model.state.tickets)", color: MCColor.blue))
                }
            }
            .padding(18)
            .mcPanel()

            ForEach(model.state.profileRows) { row in
                HStack(alignment: .top, spacing: 12) {
                    Circle()
                        .fill(row.tint.opacity(0.2))
                        .frame(width: 38, height: 38)
                    VStack(alignment: .leading, spacing: 6) {
                        Text(row.title)
                            .foregroundStyle(MCColor.textPrimary)
                        Text(row.summary)
                            .font(.subheadline)
                            .foregroundStyle(MCColor.textSecondary)
                    }
                    Spacer()
                    Text(row.value)
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(row.tint)
                }
                .padding(16)
                .mcPanel()
            }
        }
    }
}
