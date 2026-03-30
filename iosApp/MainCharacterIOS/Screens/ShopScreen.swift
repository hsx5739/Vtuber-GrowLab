import SwiftUI

struct ShopScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScreenContainer(title: "商店") {
                MetricCapsule(metric: StatusMetric(title: "当前星尘", value: "\(model.state.stardust)", color: MCColor.gold))

                ForEach(model.state.shopProducts) { product in
                    VStack(alignment: .leading, spacing: 10) {
                        Text(product.name)
                            .foregroundStyle(MCColor.textPrimary)
                        Text(product.summary)
                            .font(.subheadline)
                            .foregroundStyle(MCColor.textSecondary)
                        HStack {
                            Text("\(product.price) 星尘")
                                .foregroundStyle(product.tint)
                            Spacer()
                            OutlineButton(title: "购买", tint: product.tint) {
                                model.purchase(product: product)
                            }
                            .frame(width: 92)
                        }
                    }
                    .padding(16)
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
