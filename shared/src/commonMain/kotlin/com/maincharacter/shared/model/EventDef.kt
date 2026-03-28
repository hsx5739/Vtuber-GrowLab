package com.maincharacter.shared.model.legacy

import kotlinx.serialization.Serializable

@Serializable
data class EventDef(
    val id: String,
    val weightBase: Int,
    val tags: List<String> = emptyList(),
    val introText: String,
    val branches: List<EventBranch>,
    val schemaVersion: Int = 1
)

@Serializable
data class EventBranch(
    val optionId: String,
    val label: String,
    val requiresSkillId: String? = null,
    val energyCost: Int? = null,
    val rewardBundleId: String,
    val cooldownKey: String? = null,
    val hiddenUnlessMinBond: Int? = null
)
