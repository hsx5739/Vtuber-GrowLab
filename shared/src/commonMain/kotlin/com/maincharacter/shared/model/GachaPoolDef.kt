package com.maincharacter.shared.model.legacy

import kotlinx.serialization.Serializable

@Serializable
data class GachaPoolDef(
    val id: String,
    val cost: Map<String, Int>,
    val pityRule: PityRule,
    val rows: List<GachaRowDef>,
    val schemaVersion: Int = 1
)

@Serializable
data class GachaRowDef(
    val id: String,
    val poolId: String,
    val weight: Int,
    val rewardBundleId: String
)

@Serializable
data class PityRule(
    val rarePullsMax: Int,
    val rareTier: String
)
