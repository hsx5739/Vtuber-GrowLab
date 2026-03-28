package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class HostState(
    val fortune: Int = 0,
    val vitality: Int = 50,
    val mood: Int = 50,
    val bond: Int = 0,
    val focus: Int = 0,
    val activeBuffs: List<Buff> = emptyList(),
    val dailyTaskCompletionsByType: Map<String, Int> = emptyMap(),
    val dailyVerifiedTaskCount: Int = 0,
    val verificationScore: Int = 0
)

@Serializable
data class Buff(
    val buffId: String,
    val durationSec: Int,
    val startTime: Long = System.currentTimeMillis()
)

enum class StatKey {
    FORTUNE,
    VITALITY,
    MOOD,
    BOND,
    FOCUS
}

data class StatDelta(
    val key: StatKey,
    val value: Int
)
