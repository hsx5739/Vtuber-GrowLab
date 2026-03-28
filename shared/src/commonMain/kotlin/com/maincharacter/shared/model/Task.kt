package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskDef(
    val id: String,
    val name: String,
    val description: String,
    val type: TaskType,
    val category: TaskCategory,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val difficulty: TaskDifficulty = TaskDifficulty.NORMAL,
    val isWeekly: Boolean = false,
    val maxCompletionsPerDay: Int = 1,
    val diminishingReturns: Boolean = false,
    val minBond: Int = 0,
    val requiredFlags: List<String> = emptyList(),
    val requiredItems: List<String> = emptyList(),
    val baseReward: RewardBundle,
    val verificationBonus: RewardBundle? = null,
    val verificationRequired: Boolean = false,
    val verificationWindowSec: Int = 300,
    val autoVerify: Boolean = false,
    val config: TaskConfig? = null,
    val constraints: TaskConstraints? = null,
    val unlockConditions: UnlockConditions? = null
) {
    fun canComplete(completionsToday: Int): Boolean {
        return completionsToday < maxCompletionsPerDay
    }
    
    fun getEffectiveReward(isVerified: Boolean): RewardBundle {
        if (isVerified && verificationBonus != null) {
            return RewardBundle(
                currencies = baseReward.currencies + verificationBonus.currencies.mapValues { (k, v) ->
                    (baseReward.currencies[k] ?: 0) + v
                },
                statDelta = baseReward.statDelta + verificationBonus.statDelta.mapValues { (k, v) ->
                    (baseReward.statDelta[k] ?: 0) + v
                },
                items = baseReward.items + verificationBonus.items,
                buffs = baseReward.buffs + verificationBonus.buffs,
                telemetryTags = baseReward.telemetryTags + verificationBonus.telemetryTags
            )
        }
        return baseReward
    }
    
    fun getDiminishedReward(completionsToday: Int): RewardBundle {
        if (!diminishingReturns || completionsToday <= 1) {
            return baseReward
        }
        
        val factor = 1.0 / (1.0 + (completionsToday - 1) * 0.5)
        
        return RewardBundle(
            currencies = baseReward.currencies.mapValues { (_, amount) ->
                (amount * factor).toInt()
            },
            statDelta = baseReward.statDelta.mapValues { (_, delta) ->
                (delta * factor).toInt()
            },
            items = baseReward.items,
            buffs = baseReward.buffs,
            telemetryTags = baseReward.telemetryTags
        )
    }
    
    fun isUnlockable(hostState: HostState, flags: Set<String>, inventory: Set<String>): Boolean {
        val conditions = unlockConditions ?: return true
        
        if (hostState.bond < conditions.minBond) {
            return false
        }
        
        if (!conditions.requiredFlags.all { it in flags }) {
            return false
        }
        
        if (!conditions.requiredItems.all { it in inventory }) {
            return false
        }
        
        return true
    }
}

@Serializable
data class TaskInstance(
    val taskId: String,
    val state: TaskState = TaskState.LOCKED,
    val progress: TaskProgress = TaskProgress(),
    val completionsToday: Int = 0,
    val lastCompletionTime: Long = 0L,
    val lastVerificationTime: Long = 0L,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val metadata: Map<String, String> = emptyMap()
) {
    fun canStart(): Boolean {
        return state == TaskState.AVAILABLE
    }
    
    fun canComplete(): Boolean {
        return state == TaskState.IN_PROGRESS && progress.isComplete()
    }
    
    fun canVerify(): Boolean {
        return state == TaskState.COMPLETED && 
               !isVerificationExpired()
    }
    
    fun isVerificationExpired(): Boolean {
        if (state != TaskState.COMPLETED) return false
        val now = System.currentTimeMillis()
        val elapsed = (now - lastCompletionTime) / 1000
        return elapsed > 300
    }
    
    fun start(): TaskInstance {
        return copy(
            state = TaskState.IN_PROGRESS,
            startTime = System.currentTimeMillis()
        )
    }
    
    fun complete(): TaskInstance {
        return copy(
            state = TaskState.COMPLETED,
            endTime = System.currentTimeMillis(),
            lastCompletionTime = System.currentTimeMillis(),
            completionsToday = completionsToday + 1
        )
    }
    
    fun verify(): TaskInstance {
        return copy(
            state = TaskState.COMPLETED_VERIFIED,
            lastVerificationTime = System.currentTimeMillis()
        )
    }
    
    fun reset(): TaskInstance {
        return copy(
            state = TaskState.AVAILABLE,
            progress = TaskProgress(),
            completionsToday = 0,
            startTime = 0L,
            endTime = 0L
        )
    }
    
    fun updateProgress(newProgress: TaskProgress): TaskInstance {
        return copy(progress = newProgress)
    }
}

@Serializable
data class TaskProgress(
    val current: Int = 0,
    val target: Int = 1,
    val percentage: Float = 0f,
    val steps: List<ProgressStep> = emptyList()
) {
    fun isComplete(): Boolean {
        return current >= target
    }
    
    fun calculatePercentage(): Float {
        return if (target > 0) {
            (current.toFloat() / target.toFloat()) * 100f
        } else {
            100f
        }
    }
    
    fun advance(amount: Int = 1): TaskProgress {
        val newCurrent = (current + amount).coerceAtMost(target)
        return copy(
            current = newCurrent,
            percentage = calculatePercentage()
        )
    }
}

@Serializable
data class ProgressStep(
    val id: String,
    val completed: Boolean = false,
    val timestamp: Long = 0L
)

@Serializable
data class TaskConfig(
    val durationSec: Int? = null,
    val targetCount: Int? = null,
    val requiredItems: List<String>? = null,
    val dialogueId: String? = null,
    val eventId: String? = null,
    val imageRecognition: Boolean = false,
    val voiceDetection: Boolean = false
)

@Serializable
data class TaskConstraints(
    val maxAttempts: Int? = null,
    val timeLimitSec: Int? = null,
    val cooldownSec: Int = 0,
    val requiredItems: List<String> = emptyList()
)

@Serializable
data class UnlockConditions(
    val minBond: Int = 0,
    val requiredFlags: List<String> = emptyList(),
    val requiredItems: List<String> = emptyList(),
    val minLevel: Int = 0
)

enum class TaskType {
    A_SELF_REPORT,
    B_TIMER,
    C_HEALTH_DATA,
    D_DIALOGUE,
    F_IMAGE_RECOGNITION,
    G_VOICE_DETECTION,
    H_EVENT
}

enum class TaskCategory {
    DAILY,
    WEEKLY,
    ONE_TIME,
    EVENT,
    TUTORIAL
}

enum class TaskPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

enum class TaskDifficulty {
    EASY,
    NORMAL,
    HARD,
    EXPERT
}

enum class TaskState {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED,
    COMPLETED_VERIFIED,
    EXPIRED,
    CANCELLED
}
