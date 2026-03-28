package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

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
