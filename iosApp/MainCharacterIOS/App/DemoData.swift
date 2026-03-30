import SwiftUI

enum DemoData {
    static func initialState() -> AppState {
        AppState(
            nickname: "小主角",
            companionName: "星语",
            bond: 32,
            charm: 70,
            vitality: 68,
            focus: 52,
            mood: 50,
            stardust: 380,
            tickets: 2,
            streak: 7,
            signedToday: false,
            signInDays: (1...8).map {
                SignInRewardDay(day: $0, reward: $0 == 3 || $0 == 7 ? "抽奖券" : ($0 % 2 == 0 ? "亲密" : "星尘"))
            },
            dailyTasks: [
                TaskCard(id: "daily_home", title: "回到首页看看她", summary: "引导用户回到首页完成一次轻陪伴互动。", category: "亲密", progress: 0, target: 1, reward: "亲密 +2", completed: false, tint: MCColor.pink, isWeekly: false),
                TaskCard(id: "daily_chat", title: "和 AI 聊 3 句", summary: "让陪伴链路稳定出现，不做内容强校验。", category: "亲密", progress: 1, target: 3, reward: "亲密 +3", completed: false, tint: MCColor.pink, isWeekly: false),
                TaskCard(id: "daily_water", title: "喝水打卡一次", summary: "把自我照顾纳入今天的主循环。", category: "元气", progress: 0, target: 1, reward: "元气 +2", completed: false, tint: MCColor.green, isWeekly: false),
                TaskCard(id: "daily_focus", title: "专注 10 秒", summary: "先用很低门槛建立进入状态的习惯。", category: "专注", progress: 6, target: 10, reward: "专注 +1", completed: false, tint: MCColor.lavender, isWeekly: false)
            ],
            weeklyTasks: [
                TaskCard(id: "weekly_bond", title: "本周完成亲密任务 5 次", summary: "按类别累计，不拆成离散周任务。", category: "亲密", progress: 3, target: 5, reward: "抽奖券 +1", completed: false, tint: MCColor.gold, isWeekly: true),
                TaskCard(id: "weekly_vitality", title: "本周完成元气任务 3 次", summary: "鼓励持续照顾自己。", category: "元气", progress: 3, target: 3, reward: "抽奖券 +2", completed: true, tint: MCColor.green, isWeekly: true),
                TaskCard(id: "weekly_focus", title: "本周完成专注任务 4 次", summary: "把短专注做成周维度的沉淀。", category: "专注", progress: 2, target: 4, reward: "抽奖券 +1", completed: false, tint: MCColor.lavender, isWeekly: true)
            ],
            events: [
                EventCard(
                    id: "evt_growth",
                    title: "第二章 · 共练",
                    tag: "成长支线",
                    intro: "你决定陪她一起把今天这点成长做完，不急，但也不松。选择会改变属性反馈。",
                    state: "待处理",
                    tint: MCColor.pink,
                    choices: [
                        EventChoice(id: "evt_growth_focus", title: "一起加练", result: "你们把时间投入到训练里，节奏更直，反馈也更扎实。", effectText: "专注 +2 · 亲密 +2", effect: {
                            var next = $0
                            next.focus += 2
                            next.bond += 2
                            next.notice = "事件结算：专注 +2，亲密 +2"
                            return next
                        }),
                        EventChoice(id: "evt_growth_rest", title: "先休息半天", result: "你们没有赶进度，先把状态养稳，情绪回馈更柔和。", effectText: "魅力 +2 · 元气 +1", effect: {
                            var next = $0
                            next.charm += 2
                            next.vitality += 1
                            next.notice = "事件结算：魅力 +2，元气 +1"
                            return next
                        })
                    ]
                ),
                EventCard(
                    id: "evt_battle",
                    title: "第三章 · 大战前夜",
                    tag: "高潮事件",
                    intro: "前面的关系积累终于要转成并肩作战的瞬间了。",
                    state: "已解锁",
                    tint: MCColor.gold,
                    choices: [
                        EventChoice(id: "evt_battle_barrier", title: "发动屏障", result: "你稳住了节奏，她顺势完成反击，这次并肩感很完整。", effectText: "星尘 +20 · 亲密 +3", effect: {
                            var next = $0
                            next.stardust += 20
                            next.bond += 3
                            next.notice = "事件结算：星尘 +20，亲密 +3"
                            return next
                        })
                    ]
                )
            ],
            skills: [
                SkillCard(id: "skill_barrier", name: "屏障展开", rarity: "SR", summary: "在特定事件里解锁额外选项。", hint: "适合和剧情高潮事件联动。", tint: MCColor.lavender),
                SkillCard(id: "skill_echo", name: "绵语回响", rarity: "SSR", summary: "偏演出向，不强调硬数值。", hint: "提升陪伴感与主界面台词层次。", tint: MCColor.pink)
            ],
            items: [
                ItemCard(id: "item_energy", name: "元气果", count: 3, summary: "短时提升行动氛围。", actionTitle: "使用", tint: MCColor.green),
                ItemCard(id: "item_note", name: "小确幸果", count: 2, summary: "让轻彩蛋更容易出现。", actionTitle: "使用", tint: MCColor.gold),
                ItemCard(id: "item_ticket", name: "抽奖券", count: 2, summary: "用于后续抽卡。", actionTitle: "查看", tint: MCColor.lavender)
            ],
            skins: [
                SkinCard(id: "skin_ceremony", name: "星穹礼装", rarity: "SSR", summary: "当前主视觉外观。", ownedShards: 1, requiredShards: 1, isOwned: true, isEquipped: true, tint: MCColor.pink),
                SkinCard(id: "skin_school", name: "夜航校服", rarity: "SR", summary: "更偏日常陪伴的外观。", ownedShards: 18, requiredShards: 20, isOwned: false, isEquipped: false, tint: MCColor.lavender),
                SkinCard(id: "skin_morning", name: "晨雾便服", rarity: "R", summary: "轻生活流，适合签到和晨间互动。", ownedShards: 12, requiredShards: 12, isOwned: true, isEquipped: false, tint: MCColor.blue)
            ],
            shopProducts: [
                ShopProduct(id: "shop_ticket", name: "抽奖券", summary: "先把星尘换成后续抽卡资源。", price: 50, tint: MCColor.gold) {
                    guard $0.stardust >= 50 else { return nil }
                    var next = $0
                    next.stardust -= 50
                    next.tickets += 1
                    next.notice = "购买成功：抽奖券 +1"
                    return next
                },
                ShopProduct(id: "shop_gift", name: "亲密果礼盒", summary: "适合推进关系感受的轻补给。", price: 120, tint: MCColor.pink) {
                    guard $0.stardust >= 120 else { return nil }
                    var next = $0
                    next.stardust -= 120
                    next.bond += 3
                    next.notice = "购买成功：亲密 +3"
                    return next
                }
            ],
            latestGachaResults: [],
            profileRows: [
                ProfileRow(title: "健康数据", summary: "步数、睡眠等入口保留在这里。", value: "未开启", tint: MCColor.gold),
                ProfileRow(title: "多模态隐私", summary: "拍照、语音任务的说明与删除策略。", value: "查看说明", tint: MCColor.blue),
                ProfileRow(title: "陪伴音效", summary: "控制点击反馈和轻音效气氛。", value: "柔和", tint: MCColor.green)
            ],
            notice: "iOS 端已对齐 Android 的主要功能面。"
        )
    }
}
