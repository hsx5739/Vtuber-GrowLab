import SwiftUI

struct InventoryScreen: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        NavigationStack {
            ScreenContainer(title: "背包") {
                Picker("Section", selection: $model.inventorySection) {
                    ForEach(InventorySection.allCases) { section in
                        Text(section.rawValue).tag(section)
                    }
                }
                .pickerStyle(.segmented)

                switch model.inventorySection {
                case .skills:
                    ForEach(model.state.skills) { skill in
                        inventoryCard(title: skill.name, subtitle: "\(skill.rarity) · \(skill.summary)", footnote: skill.hint, tint: skill.tint)
                    }
                case .items:
                    ForEach(model.state.items) { item in
                        inventoryCard(title: item.name, subtitle: "数量 \(item.count) · \(item.summary)", footnote: item.actionTitle, tint: item.tint)
                    }
                case .skins:
                    ForEach(model.state.skins) { skin in
                        VStack(alignment: .leading, spacing: 10) {
                            inventoryCard(
                                title: skin.name,
                                subtitle: "\(skin.rarity) · \(skin.summary)",
                                footnote: skin.isOwned ? (skin.isEquipped ? "当前穿戴" : "已拥有") : "碎片 \(skin.ownedShards)/\(skin.requiredShards)",
                                tint: skin.tint
                            )
                            if skin.isOwned {
                                OutlineButton(title: skin.isEquipped ? "已穿戴" : "穿戴", tint: skin.tint) {
                                    model.equipSkin(id: skin.id)
                                }
                            }
                        }
                    }
                }

                Button {
                    model.activeSheet = .shop
                } label: {
                    HStack {
                        Image(systemName: "bag.fill")
                        Text("前往商店")
                        Spacer()
                        Text("\(model.state.stardust) 星尘")
                    }
                    .padding(16)
                    .foregroundStyle(MCColor.textPrimary)
                    .mcPanel()
                }
                .buttonStyle(.plain)
            }
            .toolbar(.hidden, for: .navigationBar)
        }
    }

    private func inventoryCard(title: String, subtitle: String, footnote: String, tint: Color) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .foregroundStyle(MCColor.textPrimary)
            Text(subtitle)
                .font(.subheadline)
                .foregroundStyle(MCColor.textSecondary)
            Text(footnote)
                .font(.caption.weight(.semibold))
                .foregroundStyle(tint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .mcPanel()
    }
}
