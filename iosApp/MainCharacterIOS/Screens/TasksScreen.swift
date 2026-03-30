import SwiftUI

struct TasksScreen: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        ScreenContainer(title: "任务") {
            taskSection(title: "每日任务", tasks: model.state.dailyTasks, weekly: false)
            taskSection(title: "每周任务", tasks: model.state.weeklyTasks, weekly: true)
        }
    }

    private func taskSection(title: String, tasks: [TaskCard], weekly: Bool) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(title)
                .font(.headline)
                .foregroundStyle(MCColor.textPrimary)
            ForEach(tasks) { task in
                VStack(alignment: .leading, spacing: 10) {
                    HStack(alignment: .top) {
                        VStack(alignment: .leading, spacing: 6) {
                            Text(task.title)
                                .foregroundStyle(MCColor.textPrimary)
                            Text(task.summary)
                                .font(.subheadline)
                                .foregroundStyle(MCColor.textSecondary)
                        }
                        Spacer()
                        Text(task.category)
                            .font(.caption.weight(.semibold))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(task.tint.opacity(0.18), in: Capsule())
                            .foregroundStyle(task.tint)
                    }
                    ProgressView(value: Double(task.progress), total: Double(task.target))
                        .tint(task.tint)
                    HStack {
                        Text("奖励：\(task.reward)")
                            .font(.subheadline)
                            .foregroundStyle(MCColor.textSecondary)
                        Spacer()
                        if weekly {
                            OutlineButton(title: "领取", tint: task.tint) {
                                model.claimWeekly(id: task.id)
                            }
                            .frame(width: 92)
                        } else {
                            PrimaryButton(title: task.completed ? "已完成" : "完成", tint: task.tint) {
                                model.completeTask(id: task.id)
                            }
                            .frame(width: 92)
                        }
                    }
                }
                .padding(16)
                .mcPanel()
            }
        }
    }
}
