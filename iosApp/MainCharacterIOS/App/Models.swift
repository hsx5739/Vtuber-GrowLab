import Foundation
import SwiftUI

enum HomeSheet: String, Identifiable {
    case signIn
    case gacha
    case shop

    var id: String { rawValue }
}

enum InventorySection: String, CaseIterable, Identifiable {
    case skills = "技能"
    case items = "道具"
    case skins = "外观"

    var id: String { rawValue }
}

struct StatusMetric: Identifiable {
    let id = UUID()
    let title: String
    let value: String
    let color: Color
}

struct SignInRewardDay: Identifiable {
    let id = UUID()
    let day: Int
    let reward: String
}

struct TaskCard: Identifiable {
    let id: String
    let title: String
    let summary: String
    let category: String
    let progress: Int
    let target: Int
    let reward: String
    var completed: Bool
    let tint: Color
    let isWeekly: Bool
}

struct EventChoice: Identifiable {
    let id: String
    let title: String
    let result: String
    let effectText: String
    let effect: (AppState) -> AppState
}

struct EventCard: Identifiable {
    let id: String
    let title: String
    let tag: String
    let intro: String
    let state: String
    let tint: Color
    let choices: [EventChoice]
}

struct SkillCard: Identifiable {
    let id: String
    let name: String
    let rarity: String
    let summary: String
    let hint: String
    let tint: Color
}

struct ItemCard: Identifiable {
    let id: String
    let name: String
    let count: Int
    let summary: String
    let actionTitle: String
    let tint: Color
}

struct SkinCard: Identifiable {
    let id: String
    let name: String
    let rarity: String
    let summary: String
    let ownedShards: Int
    let requiredShards: Int
    let isOwned: Bool
    var isEquipped: Bool
    let tint: Color
}

struct ShopProduct: Identifiable {
    let id: String
    let name: String
    let summary: String
    let price: Int
    let tint: Color
    let apply: (AppState) -> AppState?
}

struct GachaReward: Identifiable {
    let id = UUID()
    let title: String
    let detail: String
    let rarity: String
    let tint: Color
}

struct ProfileRow: Identifiable {
    let id = UUID()
    let title: String
    let summary: String
    let value: String
    let tint: Color
}

struct AppState {
    var nickname: String
    var companionName: String
    var bond: Int
    var charm: Int
    var vitality: Int
    var focus: Int
    var mood: Int
    var stardust: Int
    var tickets: Int
    var streak: Int
    var signedToday: Bool
    var signInDays: [SignInRewardDay]
    var dailyTasks: [TaskCard]
    var weeklyTasks: [TaskCard]
    var events: [EventCard]
    var skills: [SkillCard]
    var items: [ItemCard]
    var skins: [SkinCard]
    var shopProducts: [ShopProduct]
    var latestGachaResults: [GachaReward]
    var profileRows: [ProfileRow]
    var notice: String?
}
