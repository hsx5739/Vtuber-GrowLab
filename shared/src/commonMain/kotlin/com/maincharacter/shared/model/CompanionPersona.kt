package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class CompanionPersona(
    val id: String,
    val name: String,
    val personality: Personality,
    val traits: List<Trait>,
    val preferences: Preferences,
    val backstory: String,
    val relationshipLevel: RelationshipLevel,
    val dialogueStyle: DialogueStyle,
    val moodStates: Map<MoodState, MoodBehavior>,
    val specialInterests: List<String>,
    val forbiddenTopics: List<String>,
    val systemPrompt: SystemPrompt
) {
    fun getMoodBehavior(mood: MoodState): MoodBehavior {
        return moodStates[mood] ?: MoodBehavior.default()
    }
    
    fun hasTrait(trait: Trait): Boolean {
        return traits.contains(trait)
    }
    
    fun isTopicAllowed(topic: String): Boolean {
        return !forbiddenTopics.any { forbidden ->
            topic.contains(forbidden, ignoreCase = true)
        }
    }
    
    fun getRelationshipDescription(): String {
        return when (relationshipLevel) {
            RelationshipLevel.STRANGER -> "陌生人"
            RelationshipLevel.ACQUAINTANCE -> "熟人"
            RelationshipLevel.FRIEND -> "朋友"
            RelationshipLevel.CLOSE_FRIEND -> "好朋友"
            RelationshipLevel.PARTNER -> "伴侣"
            RelationshipLevel.SOULMATE -> "灵魂伴侣"
        }
    }
}

@Serializable
data class Personality(
    val openness: Float,
    val conscientiousness: Float,
    val extraversion: Float,
    val agreeableness: Float,
    val neuroticism: Float
) {
    companion object {
        fun createBalanced(): Personality {
            return Personality(
                openness = 0.5f,
                conscientiousness = 0.5f,
                extraversion = 0.5f,
                agreeableness = 0.5f,
                neuroticism = 0.5f
            )
        }
        
        fun createFriendly(): Personality {
            return Personality(
                openness = 0.7f,
                conscientiousness = 0.6f,
                extraversion = 0.8f,
                agreeableness = 0.9f,
                neuroticism = 0.3f
            )
        }
        
        fun createSerious(): Personality {
            return Personality(
                openness = 0.4f,
                conscientiousness = 0.9f,
                extraversion = 0.3f,
                agreeableness = 0.5f,
                neuroticism = 0.4f
            )
        }
    }
    
    fun getDominantTrait(): String {
        val traits = mapOf(
            "开放性" to openness,
            "责任心" to conscientiousness,
            "外向性" to extraversion,
            "宜人性" to agreeableness,
            "神经质" to neuroticism
        )
        
        return traits.maxByOrNull { it.value }?.key ?: "平衡"
    }
}

@Serializable
data class Trait(
    val id: String,
    val name: String,
    val description: String,
    val category: TraitCategory
)

enum class TraitCategory {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}

@Serializable
data class Preferences(
    val topics: List<String>,
    val activities: List<String>,
    val conversationStyle: ConversationStyle,
    val humorLevel: Int,
    val formalityLevel: Int
)

enum class ConversationStyle {
    CASUAL,
    FORMAL,
    MIXED
}

@Serializable
data class DialogueStyle(
    val greetingTemplates: List<String>,
    val farewellTemplates: List<String>,
    val responsePatterns: Map<String, List<String>>,
    val emojiUsage: EmojiUsage,
    val sentenceLength: SentenceLength
)

enum class EmojiUsage {
    FREQUENT,
    MODERATE,
    RARE,
    NONE
}

enum class SentenceLength {
    SHORT,
    MEDIUM,
    LONG,
    VARIED
}

@Serializable
data class MoodBehavior(
    val moodState: MoodState,
    val greetingPrefix: String,
    val responseTone: ResponseTone,
    val energyLevel: EnergyLevel,
    val topicPreferences: List<String>,
    val avoidTopics: List<String>
) {
    companion object {
        fun default(): MoodBehavior {
            return MoodBehavior(
                moodState = MoodState.NEUTRAL,
                greetingPrefix = "",
                responseTone = ResponseTone.NEUTRAL,
                energyLevel = EnergyLevel.NORMAL,
                topicPreferences = emptyList(),
                avoidTopics = emptyList()
            )
        }
    }
}

enum class MoodState {
    HAPPY,
    SAD,
    ANGRY,
    ANXIOUS,
    EXCITED,
    BORED,
    NEUTRAL
}

enum class ResponseTone {
    ENTHUSIASTIC,
    CALM,
    CONCERNED,
    PLAYFUL,
    SERIOUS,
    NEUTRAL
}

enum class EnergyLevel {
    LOW,
    NORMAL,
    HIGH
}

enum class RelationshipLevel {
    STRANGER,
    ACQUAINTANCE,
    FRIEND,
    CLOSE_FRIEND,
    PARTNER,
    SOULMATE
}

@Serializable
data class SystemPrompt(
    val coreInstructions: String,
    val roleDefinition: String,
    val behaviorGuidelines: List<String>,
    val safetyGuidelines: List<String>,
    val contextAwareness: ContextAwareness,
    val responseConstraints: ResponseConstraints,
    val fallbackResponses: FallbackResponses
)

@Serializable
data class ContextAwareness(
    val rememberPastConversations: Boolean,
    val trackUserPreferences: Boolean,
    val adaptToMood: Boolean,
    val considerTimeOfDay: Boolean,
    val considerRelationshipLevel: Boolean
)

@Serializable
data class ResponseConstraints(
    val maxLength: Int,
    val minResponseTime: Int,
    val maxResponseTime: Int,
    val allowedTopics: List<String>,
    val forbiddenTopics: List<String>,
    val formalityLevel: Int
)

@Serializable
data class FallbackResponses(
    val greeting: List<String>,
    val farewell: List<String>,
    val unknownInput: List<String>,
    val error: List<String>,
    val timeout: List<String>,
    val offline: List<String>
) {
    fun getRandomResponse(type: FallbackType): String {
        val responses = when (type) {
            FallbackType.GREETING -> greeting
            FallbackType.FAREWELL -> farewell
            FallbackType.UNKNOWN_INPUT -> unknownInput
            FallbackType.ERROR -> error
            FallbackType.TIMEOUT -> timeout
            FallbackType.OFFLINE -> offline
        }
        
        return if (responses.isNotEmpty()) {
            responses.random()
        } else {
            "我理解了"
        }
    }
}

enum class FallbackType {
    GREETING,
    FAREWELL,
    UNKNOWN_INPUT,
    ERROR,
    TIMEOUT,
    OFFLINE
}

@Serializable
data class CompanionPresentation(
    val personaId: String,
    val currentMood: MoodState,
    val energyLevel: EnergyLevel,
    val relationshipLevel: RelationshipLevel,
    val lastInteractionTime: Long,
    val interactionCount: Int,
    val moodHistory: List<MoodRecord>,
    val userPreferences: UserPreferences
)

@Serializable
data class MoodRecord(
    val mood: MoodState,
    val timestamp: Long,
    val reason: String,
    val intensity: Float
)

@Serializable
data class UserPreferences(
    val preferredTopics: List<String>,
    val conversationStyle: ConversationStyle,
    val interactionFrequency: InteractionFrequency,
    val humorPreference: HumorLevel,
    val formalityPreference: Int
)

enum class InteractionFrequency {
    RARE,
    OCCASIONAL,
    REGULAR,
    FREQUENT
}

enum class HumorLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}