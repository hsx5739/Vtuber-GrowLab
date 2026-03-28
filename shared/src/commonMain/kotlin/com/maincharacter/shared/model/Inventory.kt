package com.maincharacter.shared.model.legacy

import kotlinx.serialization.Serializable

@Serializable
data class Inventory(
    val skills: Map<String, SkillInstance> = emptyMap(),
    val fruits: Map<String, Int> = emptyMap(),
    val skins: Map<String, SkinInstance> = emptyMap(),
    val shards: Map<String, Int> = emptyMap(),
    val cosmetics: Map<String, Int> = emptyMap()
)

@Serializable
data class SkillInstance(
    val skillId: String,
    val unlocked: Boolean = true,
    val lastUsedAt: Long? = null
)

@Serializable
data class SkinInstance(
    val skinId: String,
    val owned: Boolean = true,
    val equipped: Boolean = false
)

@Serializable
data class TaskInstance(
    val taskId: String,
    val status: TaskStatus = TaskStatus.LOCKED,
    val completedAt: Long? = null,
    val verifiedAt: Long? = null
)

enum class TaskStatus {
    LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED, COMPLETED_VERIFIED
}

@Serializable
data class EventOffer(
    val eventId: String,
    val branches: List<EventBranch>,
    val expiresAt: Long,
    val cooldownUntil: Long? = null
)

@Serializable
data class SignInState(
    val lastSignDate: String? = null,
    val streak: Int = 0,
    val monthSignedBits: List<Int> = emptyList()
)

@Serializable
data class GachaState(
    val pityCounters: Map<String, Int> = emptyMap(),
    val history: List<GachaPullRecord> = emptyList()
)

@Serializable
data class GachaPullRecord(
    val pullId: String,
    val poolId: String,
    val timestamp: Long,
    val rewards: List<String>
)

@Serializable
data class CompanionPresentation(
    val equippedSkinId: String = "skin_default",
    val idleVariant: String = "default"
)
