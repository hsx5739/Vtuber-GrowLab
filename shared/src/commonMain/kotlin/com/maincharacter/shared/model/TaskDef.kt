package com.maincharacter.shared.model.legacy

import kotlinx.serialization.Serializable

@Serializable
data class TaskDef(
    val id: String,
    val taskType: TaskType,
    val title: String,
    val desc: String = "",
    val rewardBundleId: String,
    val rewardVerifiedBundleId: String? = null,
    val group: TaskGroup = TaskGroup.DAILY,
    val maxCompletionsPerDay: Int? = null,
    val diminishingReturns: Boolean = false,
    val handlerParams: Map<String, String> = emptyMap(),
    val unlockCondition: UnlockCondition? = null,
    val countsForWeekly: Boolean = false,
    val schemaVersion: Int = 1
)

enum class TaskType {
    A, B, C, D, E, F, G, H
}

enum class TaskGroup {
    DAILY, WEEKLY, STORY
}

@Serializable
data class UnlockCondition(
    val minBond: Int? = null,
    val requiredFlags: List<String> = emptyList(),
    val requiredItems: List<String> = emptyList()
)
