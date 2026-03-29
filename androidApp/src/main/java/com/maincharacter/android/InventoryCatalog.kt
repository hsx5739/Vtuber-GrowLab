package com.maincharacter.android

import androidx.compose.ui.graphics.Color
import com.maincharacter.shared.model.CurrencyType
import com.maincharacter.shared.model.Inventory
import com.maincharacter.shared.model.ItemState
import com.maincharacter.shared.model.ShardState
import com.maincharacter.shared.model.SkillCardState
import com.maincharacter.shared.model.SkinState
import com.maincharacter.shared.model.SkinUnlockSource

internal object InventoryCatalog {
    const val skillBarrier = "skill_barrier_field"
    const val skillFortune = "skill_fortune_shift"
    const val skillWhisper = "skill_whisper_echo"

    const val itemEnergyPotion = "item_energy_potion"
    const val itemLuckyNote = "item_lucky_note"
    const val itemLotteryTicket = "lottery_ticket"

    const val skinCeremony = "skin_starlight_ceremony"
    const val skinNightSchool = "skin_night_school"
    const val skinMorningCasual = "skin_morning_casual"

    const val wardrobeMigrationVersion = "wardrobe_migration_v1"
    const val imageResName = "imageResName"
    const val gachaRewardType = "gachaRewardType"
    const val duplicateRule = "duplicateRule"
    const val duplicateTargetItemId = "duplicateTargetItemId"

    val skillDefinitions = linkedMapOf(
        skillBarrier to SkillUiDefinition(
            name = "屏障展开",
            rarity = "SR",
            typeLabel = "主动技能",
            description = "在特定随机事件中解锁“发动屏障”选项，适合做短演出和小额奖励加成。",
            eventHint = "与 `evt_sandstorm_lite` 这类天气异象事件联动，后续可加入冷却和每日次数。",
            accent = Color(0xFFD6C7FF)
        ),
        skillFortune to SkillUiDefinition(
            name = "幸运微调",
            rarity = "R",
            typeLabel = "被动技能",
            description = "轻微提升惊喜型事件和好运叙事的触发权重，更像系统偷偷把风向拨向你。",
            eventHint = "适合和气运彩蛋、签到后的小型事件联动，不需要强交互也能体现价值。",
            accent = Color(0xFFFFD66E)
        ),
        skillWhisper to SkillUiDefinition(
            name = "絮语回响",
            rarity = "SSR",
            typeLabel = "演出技能",
            description = "不提供硬数值，主要解锁特殊台词、主页点击反馈和更完整的陪伴演出。",
            eventHint = "更适合高羁绊事件或夜间纸条类剧情，强化陪伴而不是战斗感。",
            accent = Color(0xFFF7B6D1)
        )
    )

    val itemDefinitions = linkedMapOf(
        itemEnergyPotion to ItemUiDefinition(
            name = "元气果",
            shortLabel = "果",
            description = "短时提升行动感与任务奖励氛围，适合在准备刷任务前使用。",
            effectHint = "数小时内能量反馈更积极",
            actionLabel = "使用",
            isConsumable = true,
            accent = Color(0xFFC8FF9B)
        ),
        itemLuckyNote to ItemUiDefinition(
            name = "小确幸果",
            shortLabel = "运",
            description = "让气运类文案和小彩蛋更活跃一点，但不和现实金钱或彩票挂钩。",
            effectHint = "短时提高气运表现",
            actionLabel = "使用",
            isConsumable = true,
            accent = Color(0xFFFFE37A)
        ),
        itemLotteryTicket to ItemUiDefinition(
            name = "抽奖券",
            shortLabel = "券",
            description = "由每周任务分档奖励产出，用于后续抽奖系统消耗。",
            effectHint = "周任务奖励资产",
            actionLabel = "查看",
            isConsumable = false,
            accent = Color(0xFFD6C7FF)
        )
    )

    val skinDefinitions = linkedMapOf(
        skinCeremony to SkinUiDefinition(
            name = "星巡礼装",
            rarity = "SSR",
            description = "当前主视觉使用的陪伴者外观，适合作为首页舞台页的基础皮肤。",
            shardsRequired = 1,
            defaultUnlocked = true,
            defaultEquipped = true,
            imageRes = R.drawable.companion_pose_2,
            accent = Color(0xFFF7B6D1),
            metadata = mapOf(
                imageResName to "companion_pose_2",
                gachaRewardType to "skin",
                duplicateRule to "skin_to_shards"
            )
        ),
        skinNightSchool to SkinUiDefinition(
            name = "夜航校服",
            rarity = "SR",
            description = "更贴近日常陪伴气质的皮肤，可在聊天和任务页强调温柔陪伴感。",
            shardsRequired = 20,
            defaultUnlocked = false,
            defaultEquipped = false,
            imageRes = R.drawable.m21,
            accent = Color(0xFFD6C7FF),
            metadata = mapOf(
                imageResName to "m21",
                gachaRewardType to "skin",
                duplicateRule to "skin_to_shards"
            )
        ),
        skinMorningCasual to SkinUiDefinition(
            name = "晨雾便服",
            rarity = "R",
            description = "轻松、治愈、偏生活流的装束，适合签到和早安问候场景。",
            shardsRequired = 12,
            defaultUnlocked = true,
            defaultEquipped = false,
            imageRes = R.drawable.m22,
            accent = Color(0xFF9ED8FF),
            metadata = mapOf(
                imageResName to "m22",
                gachaRewardType to "skin",
                duplicateRule to "skin_to_shards"
            )
        )
    )

    val migratedWardrobeSkinIds = setOf(skinNightSchool, skinMorningCasual)
    val skillDuplicateConversion = mapOf(
        "N" to itemEnergyPotion,
        "R" to itemEnergyPotion,
        "SR" to itemLuckyNote,
        "SSR" to itemLotteryTicket
    )
}

internal data class SkillUiDefinition(
    val name: String,
    val rarity: String,
    val typeLabel: String,
    val description: String,
    val eventHint: String,
    val accent: Color
)

internal data class ItemUiDefinition(
    val name: String,
    val shortLabel: String,
    val description: String,
    val effectHint: String,
    val actionLabel: String,
    val isConsumable: Boolean,
    val accent: Color
)

internal data class SkinUiDefinition(
    val name: String,
    val rarity: String,
    val description: String,
    val shardsRequired: Int,
    val defaultUnlocked: Boolean,
    val defaultEquipped: Boolean,
    val imageRes: Int,
    val accent: Color,
    val metadata: Map<String, String>
)

internal fun defaultInventoryState(ticketCount: Int = demoAccountContext.lotteryTicketCount): Inventory {
    val now = System.currentTimeMillis()
    val skills = linkedMapOf(
        InventoryCatalog.skillBarrier to SkillCardState(
            skillCardId = InventoryCatalog.skillBarrier,
            isUnlocked = true,
            unlockTime = now,
            level = 3,
            experience = 40,
            usageCount = 2
        ),
        InventoryCatalog.skillFortune to SkillCardState(
            skillCardId = InventoryCatalog.skillFortune,
            isUnlocked = true,
            unlockTime = now,
            level = 2,
            experience = 20,
            usageCount = 5
        ),
        InventoryCatalog.skillWhisper to SkillCardState(
            skillCardId = InventoryCatalog.skillWhisper,
            isUnlocked = true,
            unlockTime = now,
            level = 1,
            experience = 0,
            usageCount = 1
        )
    )
    val items = linkedMapOf(
        InventoryCatalog.itemEnergyPotion to ItemState(
            itemId = InventoryCatalog.itemEnergyPotion,
            count = 3,
            isConsumable = true
        ),
        InventoryCatalog.itemLuckyNote to ItemState(
            itemId = InventoryCatalog.itemLuckyNote,
            count = 2,
            isConsumable = true
        ),
        InventoryCatalog.itemLotteryTicket to ItemState(
            itemId = InventoryCatalog.itemLotteryTicket,
            count = ticketCount,
            isConsumable = false
        )
    )
    val skins = linkedMapOf(
        InventoryCatalog.skinCeremony to SkinState(
            skinId = InventoryCatalog.skinCeremony,
            isUnlocked = true,
            unlockTime = now,
            unlockSource = SkinUnlockSource.DEFAULT,
            shardCount = 1,
            isEquipped = true,
            equipTime = now,
            metadata = InventoryCatalog.skinDefinitions.getValue(InventoryCatalog.skinCeremony).metadata
        ),
        InventoryCatalog.skinNightSchool to SkinState(
            skinId = InventoryCatalog.skinNightSchool,
            isUnlocked = false,
            shardCount = 0,
            metadata = InventoryCatalog.skinDefinitions.getValue(InventoryCatalog.skinNightSchool).metadata
        ),
        InventoryCatalog.skinMorningCasual to SkinState(
            skinId = InventoryCatalog.skinMorningCasual,
            isUnlocked = true,
            unlockTime = now,
            unlockSource = SkinUnlockSource.DEFAULT,
            shardCount = 0,
            metadata = InventoryCatalog.skinDefinitions.getValue(InventoryCatalog.skinMorningCasual).metadata
        )
    )
    val shards = linkedMapOf(
        InventoryCatalog.skinCeremony to ShardState(skinId = InventoryCatalog.skinCeremony, count = 1),
        InventoryCatalog.skinNightSchool to ShardState(skinId = InventoryCatalog.skinNightSchool, count = 0),
        InventoryCatalog.skinMorningCasual to ShardState(skinId = InventoryCatalog.skinMorningCasual, count = 0)
    )
    return Inventory(
        userId = demoAccountContext.characterId,
        skillCards = skills,
        items = items,
        skins = skins,
        shards = shards,
        currencies = mapOf(
            CurrencyType.GACHA_TICKET to ticketCount,
            CurrencyType.STAR_DUST to 1_000_000
        ),
        equippedSkinId = InventoryCatalog.skinCeremony,
        metadata = mapOf(
            InventoryCatalog.wardrobeMigrationVersion to "true"
        )
    )
}

internal fun currentInventorySkills(inventory: Inventory): List<InventorySkill> {
    return InventoryCatalog.skillDefinitions.map { (skillId, definition) ->
        val state = inventory.skillCards[skillId]
        val stateLabel = if (state?.isUnlocked == true) {
            "Lv.${state.level} · 已解锁"
        } else {
            "未解锁"
        }
        InventorySkill(
            id = skillId,
            name = definition.name,
            rarity = definition.rarity,
            typeLabel = definition.typeLabel,
            stateLabel = stateLabel,
            description = definition.description,
            eventHint = definition.eventHint,
            accent = definition.accent
        )
    }
}

internal fun currentInventoryItems(inventory: Inventory): List<InventoryItem> {
    return InventoryCatalog.itemDefinitions.map { (itemId, definition) ->
        val state = inventory.items[itemId]
        InventoryItem(
            id = itemId,
            name = definition.name,
            shortLabel = definition.shortLabel,
            count = (state?.count ?: 0).toString(),
            description = definition.description,
            effectHint = definition.effectHint,
            actionLabel = definition.actionLabel,
            accent = definition.accent
        )
    }
}

internal fun currentInventorySkins(inventory: Inventory): List<InventorySkin> {
    val equippedSkinId = inventory.equippedSkinId
    return InventoryCatalog.skinDefinitions.map { (skinId, definition) ->
        val skinState = inventory.skins[skinId]
        val shardCount = inventory.shards[skinId]?.count ?: skinState?.shardCount ?: 0
        val isUnlocked = skinState?.isUnlocked == true
        val isEquipped = equippedSkinId == skinId && isUnlocked
        val actionLabel = when {
            isEquipped -> "已穿戴"
            isUnlocked -> "去换上"
            else -> "差 ${(definition.shardsRequired - shardCount).coerceAtLeast(0)} 碎片"
        }
        InventorySkin(
            id = skinId,
            name = definition.name,
            rarity = definition.rarity,
            description = definition.description,
            shardsOwned = shardCount,
            shardsRequired = definition.shardsRequired,
            owned = isUnlocked,
            actionLabel = actionLabel,
            accent = definition.accent,
            imageRes = definition.imageRes
        )
    }
}

internal fun currentEquippedInventorySkin(inventory: Inventory): InventorySkin {
    return currentInventorySkins(inventory).firstOrNull { it.id == inventory.equippedSkinId }
        ?: currentInventorySkins(inventory).first()
}

internal fun currentInventoryTicketCount(inventory: Inventory): Int {
    return inventory.items[InventoryCatalog.itemLotteryTicket]?.count
        ?: inventory.currencies[CurrencyType.GACHA_TICKET]
        ?: 0
}
