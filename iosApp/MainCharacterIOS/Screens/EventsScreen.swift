import SwiftUI

struct EventsScreen: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        ScreenContainer(title: "事件") {
            ForEach(model.state.events) { event in
                VStack(alignment: .leading, spacing: 14) {
                    HStack {
                        VStack(alignment: .leading, spacing: 6) {
                            Text(event.title)
                                .foregroundStyle(MCColor.textPrimary)
                            Text(event.tag)
                                .font(.caption.weight(.semibold))
                                .foregroundStyle(event.tint)
                        }
                        Spacer()
                        Text(event.state)
                            .font(.caption)
                            .foregroundStyle(MCColor.textSecondary)
                    }
                    Text(event.intro)
                        .foregroundStyle(MCColor.textSecondary)

                    ForEach(event.choices) { choice in
                        Button {
                            model.choose(event: event, choice: choice)
                        } label: {
                            VStack(alignment: .leading, spacing: 6) {
                                Text(choice.title)
                                    .foregroundStyle(MCColor.textPrimary)
                                Text(choice.result)
                                    .font(.subheadline)
                                    .foregroundStyle(MCColor.textSecondary)
                                Text(choice.effectText)
                                    .font(.caption.weight(.semibold))
                                    .foregroundStyle(event.tint)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(14)
                            .background(event.tint.opacity(0.12), in: RoundedRectangle(cornerRadius: 18, style: .continuous))
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(18)
                .mcPanel()
            }
        }
    }
}
