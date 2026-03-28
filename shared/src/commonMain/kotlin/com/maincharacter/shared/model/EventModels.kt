package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class EventDef(
    val id: String,
    val name: String,
    val description: String,
    val type: EventType,
    val rarity: EventRarity,
    val tags: List<String>,
    val trigger: EventTrigger,
    val intro: EventIntro,
    val choices: List<EventChoice>,
    val outcomes: Map<String, EventOutcome>,
    val cooldown: EventCooldown,
    val requirements: EventRequirements,
    val rewards: EventRewards,
    val skillInteractions: List<SkillInteraction>,
    val metadata: EventMetadata
) {
    fun getOutcome(choiceId: String): EventOutcome? {
        return outcomes[choiceId]
    }
    
    fun hasTag(tag: String): Boolean {
        return tags.contains(tag)
    }
    
    fun meetsRequirements(hostState: HostState): Boolean {
        if (requirements.minBond > hostState.bond) return false
        if (requirements.maxBond < hostState.bond) return false
        if (requirements.minFortune > hostState.fortune) return false
        if (requirements.minVitality > hostState.vitality) return false
        if (requirements.minMood > hostState.mood) return false
        
        if (requirements.requiredTags.isNotEmpty()) {
            val hasAllTags = requirements.requiredTags.all { tag ->
                tags.contains(tag)
            }
            if (!hasAllTags) return false
        }
        
        return true
    }
    
    fun isAvailable(
        hostState: HostState,
        lastTriggerTime: Long
    ): Boolean {
        if (!meetsRequirements(hostState)) return false
        
        val currentTime = System.currentTimeMillis()
        val timeSinceLastTrigger = currentTime - lastTriggerTime
        
        if (timeSinceLastTrigger < cooldown.minCooldownMs) return false
        
        return true
    }
}

@Serializable
data class EventTrigger(
    val type: TriggerType,
    val conditions: List<TriggerCondition>,
    val probability: Float,
    val maxOccurrences: Int,
    val timeWindow: Long
)

enum class TriggerType {
    TIME_BASED,
    INTERACTION_BASED,
    ATTRIBUTE_BASED,
    RANDOM,
    CONDITIONAL
}

@Serializable
data class TriggerCondition(
    val type: ConditionType,
    val value: String,
    val operator: ConditionOperator
)

enum class ConditionType {
    ATTRIBUTE_VALUE,
    TIME_OF_DAY,
    DAY_OF_WEEK,
    COMPLETED_TASKS,
    EVENT_COUNT,
    SKILL_OWNED
}

enum class ConditionOperator {
    EQUAL,
    GREATER_THAN,
    LESS_THAN,
    GREATER_OR_EQUAL,
    LESS_OR_EQUAL,
    CONTAINS,
    NOT_CONTAINS
}

@Serializable
data class EventIntro(
    val title: String,
    val description: String,
    val imageUrl: String?,
    val audioUrl: String?,
    val mood: MoodState,
    val energyLevel: EnergyLevel
)

@Serializable
data class EventChoice(
    val id: String,
    val text: String,
    val description: String,
    val icon: String?,
    val energyCost: Int,
    val skillRequired: String?,
    val skillBonus: String?,
    val moodModifier: Int,
    val isDefault: Boolean
)

@Serializable
data class EventOutcome(
    val id: String,
    val description: String,
    val imageUrl: String?,
    val audioUrl: String?,
    val statChanges: List<StatChange>,
    val currencyChanges: List<CurrencyChange>,
    val itemRewards: List<ItemReward>,
    val skillRewards: List<SkillReward>,
    val relationshipChange: Int,
    val moodChange: Int,
    val followUpEvent: String?,
    val specialEffects: List<SpecialEffect>
)

@Serializable
data class StatChange(
    val key: StatKey,
    val delta: Int,
    val min: Int,
    val max: Int,
    val reason: String
)

@Serializable
data class CurrencyChange(
    val currency: CurrencyType,
    val amount: Int,
    val reason: String
)

@Serializable
data class ItemReward(
    val itemId: String,
    val quantity: Int,
    val reason: String
)

@Serializable
data class SkillReward(
    val skillId: String,
    val reason: String
)

@Serializable
data class SpecialEffect(
    val type: EffectType,
    val value: String,
    val duration: Long
)

enum class EffectType {
    BUFF,
    DEBUFF,
    TRIGGER_EVENT,
    UNLOCK_SKIN,
    UNLOCK_SKILL,
    MODIFY_ATTRIBUTE
}

@Serializable
data class EventCooldown(
    val minCooldownMs: Long,
    val maxCooldownMs: Long,
    val globalCooldownMs: Long,
    val perDayLimit: Int,
    val perWeekLimit: Int
)

@Serializable
data class EventRequirements(
    val minBond: Int,
    val maxBond: Int,
    val minFortune: Int,
    val minVitality: Int,
    val minMood: Int,
    val requiredTags: List<String>,
    val forbiddenTags: List<String>,
    val requiredSkills: List<String>,
    val requiredItems: List<String>
)

@Serializable
data class EventRewards(
    val rewardBundleId: String?,
    val guaranteedRewards: List<RewardItem>,
    val randomRewards: List<RandomReward>,
    val milestoneRewards: Map<Int, List<RewardItem>>
)

@Serializable
data class RewardItem(
    val type: RewardType,
    val id: String,
    val quantity: Int,
    val probability: Float
)

enum class RewardType {
    CURRENCY,
    ITEM,
    SKILL,
    SKIN,
    BUFF
}

@Serializable
data class RandomReward(
    val pool: String,
    val count: Int,
    val probability: Float
)

@Serializable
data class SkillInteraction(
    val skillId: String,
    val interactionType: SkillInteractionType,
    val modifier: Float,
    val effect: String
)

enum class SkillInteractionType {
    WEIGHT_MODIFIER,
    OUTCOME_MODIFIER,
    COST_REDUCTION,
    PROBABILITY_BOOST,
    UNLOCK_OPTION
}

@Serializable
data class EventMetadata(
    val version: String,
    val author: String,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String,
    val isExperimental: Boolean,
    val isSeasonal: Boolean,
    val seasonInfo: SeasonInfo?
)

@Serializable
data class SeasonInfo(
    val seasonId: String,
    val startDate: Long,
    val endDate: Long,
    val theme: String
)

enum class EventType {
    STORY,
    RANDOM,
    SPECIAL,
    SEASONAL,
    INTERACTIVE,
    PASSIVE
}

enum class EventRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

@Serializable
data class EventOffer(
    val id: String,
    val eventId: String,
    val offerType: OfferType,
    val title: String,
    val description: String,
    val choices: List<OfferChoice>,
    val expirationTime: Long,
    val isUrgent: Boolean,
    val priority: Int,
    val metadata: OfferMetadata
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expirationTime
    }
    
    fun getRemainingTime(): Long {
        val remaining = expirationTime - System.currentTimeMillis()
        return remaining.coerceAtLeast(0L)
    }
    
    fun getUrgencyLevel(): UrgencyLevel {
        val remainingMs = getRemainingTime()
        
        return when {
            remainingMs < 3600000 -> UrgencyLevel.CRITICAL
            remainingMs < 86400000 -> UrgencyLevel.HIGH
            remainingMs < 604800000 -> UrgencyLevel.MEDIUM
            else -> UrgencyLevel.LOW
        }
    }
}

enum class OfferType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    TIMED,
    CONDITIONAL,
    SKILL_BASED
}

@Serializable
data class OfferChoice(
    val id: String,
    val text: String,
    val description: String,
    val icon: String?,
    val cost: OfferCost,
    val rewards: List<RewardItem>,
    val requirements: ChoiceRequirements,
    val isRecommended: Boolean,
    val isLocked: Boolean,
    val lockReason: String?
)

@Serializable
data class OfferCost(
    val currency: CurrencyType,
    val amount: Int,
    val energy: Int,
    val items: List<ItemCost>
)

@Serializable
data class ItemCost(
    val itemId: String,
    val quantity: Int
)

@Serializable
data class ChoiceRequirements(
    val minBond: Int,
    val minFortune: Int,
    val requiredSkills: List<String>,
    val requiredItems: List<String>
)

@Serializable
data class OfferMetadata(
    val source: String,
    val createdAt: Long,
    val expiresAt: Long,
    val isRepeatable: Boolean,
    val repeatInterval: Long,
    val maxOccurrences: Int,
    val currentOccurrences: Int
)

enum class UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Serializable
data class EventInstance(
    val id: String,
    val eventId: String,
    val state: EventState,
    val currentStage: EventStage,
    val selectedChoice: String?,
    val startTime: Long,
    val endTime: Long?,
    val statChanges: List<StatChange>,
    val rewardsReceived: List<RewardItem>,
    val metadata: InstanceMetadata
) {
    fun isComplete(): Boolean {
        return state == EventState.COMPLETED || state == EventState.ABANDONED
    }
    
    fun getDuration(): Long {
        val end = endTime ?: System.currentTimeMillis()
        return end - startTime
    }
    
    fun canProceed(): Boolean {
        return state == EventState.IN_PROGRESS && selectedChoice != null
    }
}

enum class EventState {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ABANDONED,
    EXPIRED
}

enum class EventStage {
    INTRO,
    CHOICE,
    OUTCOME,
    CONCLUSION
}

@Serializable
data class InstanceMetadata(
    val userId: String,
    val sessionId: String,
    val triggerSource: String,
    val fortuneModifier: Float,
    val skillModifiers: Map<String, Float>
)