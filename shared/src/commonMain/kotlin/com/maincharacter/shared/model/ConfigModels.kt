package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class SkinDef(
    val id: String,
    val rarity: String,
    val shardItemId: String? = null,
    val shardCost: Int? = null,
    val equipStatDelta: Map<StatKey, Int> = emptyMap(),
    val bondBonusOnEquip: Int? = null,
    val schemaVersion: Int = 1
)

@Serializable
data class ItemDef(
    val id: String,
    val kind: ItemKind,
    val maxStack: Int = 999,
    val useRewardBundleId: String? = null,
    val skillCardId: String? = null,
    val schemaVersion: Int = 1
)

enum class ItemKind {
    FRUIT, SHARD, COSMETIC, SKILL_CARD, CURRENCY_PACK
}

@Serializable
data class BuffDef(
    val id: String,
    val mergePolicy: BuffMergePolicy,
    val statMultiplier: Map<StatKey, Float> = emptyMap(),
    val statFlatBonus: Map<StatKey, Int> = emptyMap(),
    val defaultDurationSec: Int = 0,
    val schemaVersion: Int = 1
)

enum class BuffMergePolicy {
    REFRESH_DURATION, STACK_INTENSITY_MAX_3
}

@Serializable
data class LegacySkillDef(
    val id: String,
    val kind: LegacySkillKind,
    val cooldownSec: Int = 0,
    val eventWeightModifiers: Map<String, Int> = emptyMap(),
    val cosmeticIdleKey: String? = null,
    val schemaVersion: Int = 1
)

enum class LegacySkillKind {
    PASSIVE, ACTIVE, COSMETIC
}

@Serializable
data class SignInRuleDef(
    val streakDay: Int,
    val rewardBundleId: String,
    val schemaVersion: Int = 1
)
