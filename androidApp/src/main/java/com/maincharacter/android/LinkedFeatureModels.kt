package com.maincharacter.android

import androidx.compose.ui.graphics.Color

internal enum class ShopRewardType {
    ITEM,
    SKIN_SHARD,
    TICKET,
    STATUS,
    STARDUST
}

internal enum class StatusType {
    BOND,
    CHARM,
    VITALITY,
    FOCUS,
    MOOD
}

internal data class ShopProductDefinition(
    val productId: String,
    val sectionId: String,
    val name: String,
    val description: String,
    val price: Int,
    val rewardType: ShopRewardType,
    val rewardTargetId: String? = null,
    val rewardAmount: Int,
    val accent: Color,
    val badge: String? = null
)

internal data class ShopSectionDefinition(
    val sectionId: String,
    val title: String,
    val summary: String
)

internal data class GachaRewardPreview(
    val rewardType: String,
    val targetName: String,
    val amountText: String,
    val rarity: String,
    val weight: Int,
    val duplicateRule: String,
    val executionType: GachaExecutionType,
    val targetId: String,
    val minAmount: Int? = null,
    val maxAmount: Int? = null
)

internal enum class GachaExecutionType {
    SKILL,
    ITEM,
    SHARD,
    TICKET,
    STARDUST
}

internal data class GachaPoolPreview(
    val poolId: String,
    val name: String,
    val description: String,
    val ticketCost: Int,
    val featuredNote: String,
    val rewards: List<GachaRewardPreview>
)

internal val linkedShopSections = listOf(
    ShopSectionDefinition(
        sectionId = "supply",
        title = "养成补给",
        summary = "直接作用于当前角色状态的补给型商品，购买后立即写入角色状态。"
    ),
    ShopSectionDefinition(
        sectionId = "wardrobe",
        title = "外观碎片",
        summary = "商城本期只售卖外观碎片，不直接出售完整外观。"
    ),
    ShopSectionDefinition(
        sectionId = "gacha",
        title = "抽卡券",
        summary = "统一补充抽卡券库存，为后续抽卡执行链路复用同一资产口径。"
    )
)

internal val linkedShopProducts = listOf(
    ShopProductDefinition(
        productId = "shop_supply_energy_potion",
        sectionId = "supply",
        name = "体力药剂",
        description = "背包道具 +1，可在后续养成链路中消耗。",
        price = 80,
        rewardType = ShopRewardType.ITEM,
        rewardTargetId = InventoryCatalog.itemEnergyPotion,
        rewardAmount = 1,
        accent = Color(0xFFC8FF9B),
        badge = "道具"
    ),
    ShopProductDefinition(
        productId = "shop_supply_lucky_note",
        sectionId = "supply",
        name = "幸运便签",
        description = "背包道具 +1，用于事件或后续掉落转化。",
        price = 120,
        rewardType = ShopRewardType.ITEM,
        rewardTargetId = InventoryCatalog.itemLuckyNote,
        rewardAmount = 1,
        accent = Color(0xFFFFE37A),
        badge = "道具"
    ),
    ShopProductDefinition(
        productId = "shop_status_bond_pack",
        sectionId = "supply",
        name = "亲密补给",
        description = "角色状态 bond +1，购买后立即生效。",
        price = 90,
        rewardType = ShopRewardType.STATUS,
        rewardTargetId = StatusType.BOND.name,
        rewardAmount = 1,
        accent = Color(0xFFF6B7D2),
        badge = "状态"
    ),
    ShopProductDefinition(
        productId = "shop_status_focus_pack",
        sectionId = "supply",
        name = "专注补给",
        description = "角色状态 focus +1，购买后立即生效。",
        price = 110,
        rewardType = ShopRewardType.STATUS,
        rewardTargetId = StatusType.FOCUS.name,
        rewardAmount = 1,
        accent = Color(0xFFD6C7FF),
        badge = "状态"
    ),
    ShopProductDefinition(
        productId = "shop_skin_night_school_shard",
        sectionId = "wardrobe",
        name = "夜航校服碎片",
        description = "对应 skin_night_school 碎片 +1。",
        price = 600,
        rewardType = ShopRewardType.SKIN_SHARD,
        rewardTargetId = InventoryCatalog.skinNightSchool,
        rewardAmount = 1,
        accent = Color(0xFFFFD66E),
        badge = "碎片"
    ),
    ShopProductDefinition(
        productId = "shop_skin_morning_casual_shard",
        sectionId = "wardrobe",
        name = "晨雾便服碎片",
        description = "对应 skin_morning_casual 碎片 +1。",
        price = 400,
        rewardType = ShopRewardType.SKIN_SHARD,
        rewardTargetId = InventoryCatalog.skinMorningCasual,
        rewardAmount = 1,
        accent = Color(0xFF9ED8FF),
        badge = "碎片"
    ),
    ShopProductDefinition(
        productId = "shop_lottery_ticket",
        sectionId = "gacha",
        name = "抽卡券",
        description = "购买后抽卡券库存 +1，背包与抽卡页同步刷新。",
        price = 50,
        rewardType = ShopRewardType.TICKET,
        rewardTargetId = InventoryCatalog.itemLotteryTicket,
        rewardAmount = 1,
        accent = Color(0xFFD6C7FF),
        badge = "票券"
    )
)

internal val linkedGachaPools = listOf(
    GachaPoolPreview(
        poolId = "pool_shared_assets_v1",
        name = "全资产联动池",
        description = "本期不执行真实抽卡，但已经按正式规则展示资产范围、消耗与重复处理口径。",
        ticketCost = 10,
        featuredNote = "奖池覆盖物品、技能、道具、碎片与星尘，后续执行层直接复用。",
        rewards = listOf(
            GachaRewardPreview("技能", "屏障场", "按级掉落", "SR", 20, "满级重复转道具", GachaExecutionType.SKILL, InventoryCatalog.skillBarrier),
            GachaRewardPreview("道具", "体力药剂", "+1", "R", 35, "重复叠加", GachaExecutionType.ITEM, InventoryCatalog.itemEnergyPotion),
            GachaRewardPreview("碎片", "夜航校服", "+1", "SR", 18, "重复叠加", GachaExecutionType.SHARD, InventoryCatalog.skinNightSchool),
            GachaRewardPreview("物品", "抽卡券", "+1", "R", 12, "重复叠加", GachaExecutionType.TICKET, InventoryCatalog.itemLotteryTicket),
            GachaRewardPreview("星尘", "STAR_DUST", "5~100", "N", 15, "直接累加", GachaExecutionType.STARDUST, "stardust", 5, 100)
        )
    )
)
