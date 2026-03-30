import Foundation
import SwiftUI

final class AppModel: ObservableObject {
    @Published private(set) var state = DemoData.initialState()
    @Published var activeSheet: HomeSheet?
    @Published var inventorySection: InventorySection = .skills

    var homeMetrics: [StatusMetric] {
        [
            StatusMetric(title: "亲密", value: "\(state.bond)", color: MCColor.pink),
            StatusMetric(title: "魅力", value: "\(state.charm)%", color: MCColor.blue),
            StatusMetric(title: "元气", value: "\(state.vitality)", color: MCColor.green),
            StatusMetric(title: "专注", value: "\(state.focus)", color: MCColor.lavender)
        ]
    }

    func signInToday() {
        guard !state.signedToday else {
            state.notice = "今天已经签到过了。"
            return
        }
        state.signedToday = true
        state.streak += 1
        if [3, 7, 15].contains(state.streak) {
            state.tickets += 1
        }
        state.notice = "签到成功，连续 \(state.streak) 天。"
        completeTask(id: "daily_home")
    }

    func completeTask(id: String) {
        guard let index = state.dailyTasks.firstIndex(where: { $0.id == id }) else { return }
        guard !state.dailyTasks[index].completed else { return }
        let task = state.dailyTasks[index]
        state.dailyTasks[index] = TaskCard(
            id: task.id,
            title: task.title,
            summary: task.summary,
            category: task.category,
            progress: task.target,
            target: task.target,
            reward: task.reward,
            completed: true,
            tint: task.tint,
            isWeekly: task.isWeekly
        )
        applyRewardText(task.reward)
    }

    func claimWeekly(id: String) {
        guard let index = state.weeklyTasks.firstIndex(where: { $0.id == id }) else { return }
        let task = state.weeklyTasks[index]
        guard task.completed, task.progress >= task.target else {
            state.notice = "周任务未达成。"
            return
        }
        state.tickets += task.id == "weekly_vitality" ? 2 : 1
        state.weeklyTasks[index] = TaskCard(
            id: task.id,
            title: task.title,
            summary: task.summary,
            category: task.category,
            progress: task.target,
            target: task.target,
            reward: "已领取",
            completed: false,
            tint: task.tint,
            isWeekly: true
        )
        state.notice = "周奖励领取成功。"
    }

    func choose(event: EventCard, choice: EventChoice) {
        state = choice.effect(state)
        state.events.removeAll { $0.id == event.id }
    }

    func purchase(product: ShopProduct) {
        guard let next = product.apply(state) else {
            state.notice = "星尘不足。"
            return
        }
        state = next
    }

    func draw() {
        guard state.tickets > 0 else {
            state.notice = "抽奖券不足。"
            return
        }
        state.tickets -= 1
        let pool = [
            GachaReward(title: "屏障展开", detail: "新技能解锁", rarity: "SR", tint: MCColor.lavender),
            GachaReward(title: "夜航校服碎片", detail: "碎片 +1", rarity: "R", tint: MCColor.blue),
            GachaReward(title: "星尘补给", detail: "星尘 +30", rarity: "N", tint: MCColor.gold),
            GachaReward(title: "绵语回响", detail: "演出技能", rarity: "SSR", tint: MCColor.pink)
        ]
        let reward = pool.randomElement() ?? pool[0]
        state.latestGachaResults = [reward]
        switch reward.title {
        case "夜航校服碎片":
            guard let index = state.skins.firstIndex(where: { $0.id == "skin_school" }) else { break }
            let current = state.skins[index]
            let nextShard = current.ownedShards + 1
            state.skins[index] = SkinCard(
                id: current.id,
                name: current.name,
                rarity: current.rarity,
                summary: current.summary,
                ownedShards: nextShard,
                requiredShards: current.requiredShards,
                isOwned: nextShard >= current.requiredShards,
                isEquipped: current.isEquipped,
                tint: current.tint
            )
        case "星尘补给":
            state.stardust += 30
        default:
            break
        }
        state.notice = "抽取完成：\(reward.title)"
    }

    func equipSkin(id: String) {
        guard let target = state.skins.first(where: { $0.id == id }), target.isOwned else {
            state.notice = "该外观尚未拥有。"
            return
        }
        state.skins = state.skins.map { skin in
            SkinCard(
                id: skin.id,
                name: skin.name,
                rarity: skin.rarity,
                summary: skin.summary,
                ownedShards: skin.ownedShards,
                requiredShards: skin.requiredShards,
                isOwned: skin.isOwned,
                isEquipped: skin.id == id,
                tint: skin.tint
            )
        }
        state.notice = "已切换外观：\(target.name)"
    }

    func clearNotice() {
        state.notice = nil
    }

    private func applyRewardText(_ reward: String) {
        if reward.contains("亲密") {
            state.bond += 2
        } else if reward.contains("元气") {
            state.vitality += 2
        } else if reward.contains("专注") {
            state.focus += 1
        }
        state.notice = "任务完成：\(reward)"
    }
}
