package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class CompanionConfig(
    val companionId: String,
    val companionName: String,
    val defaultSkinId: String,
    val availableSkins: List<String>,
    val interactionDialogues: Map<InteractionType, List<DialogueConfig>>,
    val moodDialogues: Map<Int, List<String>>,
    val bondDialogues: Map<Int, List<String>>,
    val specialDialogues: List<SpecialDialogueConfig>,
    val interactionEffects: Map<InteractionType, List<EffectConfig>>,
    val personalityTraits: List<PersonalityTrait>,
    val voiceSettings: VoiceSettings,
    val animationSettings: AnimationSettings
) {
    fun getDialogues(
        interactionType: InteractionType,
        mood: Int,
        bondLevel: Int
    ): List<String> {
        val interactionDialogues = interactionDialogues[interactionType] ?: emptyList()
        
        val moodDialoguesForLevel = moodDialogues.filterKeys { moodLevel ->
            moodLevel <= mood
        }.values.flatten()
        
        val bondDialoguesForLevel = bondDialogues.filterKeys { level ->
            level <= bondLevel
        }.values.flatten()
        
        val allDialogues = mutableListOf<String>()
        allDialogues.addAll(interactionDialogues.map { it.dialogue })
        allDialogues.addAll(moodDialoguesForLevel)
        allDialogues.addAll(bondDialoguesForLevel)
        
        return allDialogues
    }
    
    fun getEffect(
        interactionType: InteractionType
    ): EffectConfig? {
        val effects = interactionEffects[interactionType]
        return effects?.randomOrNull()
    }
    
    fun getSpecialDialogue(
        condition: SpecialDialogueCondition
    ): SpecialDialogueConfig? {
        return specialDialogues.find { it.condition == condition }
    }
}

@Serializable
data class DialogueConfig(
    val dialogueId: String,
    val dialogue: String,
    val moodRequirement: Int? = null,
    val bondLevelRequirement: Int? = null,
    val energyRequirement: Int? = null,
    val timeOfDayRequirement: TimeOfDay? = null,
    val priority: Int = 0,
    val cooldown: Long = 0L,
    val metadata: Map<String, String> = emptyMap()
) {
    fun canTrigger(
        currentMood: Int,
        currentBondLevel: Int,
        currentEnergy: Int,
        currentTime: Long
    ): Boolean {
        if (moodRequirement != null && currentMood < moodRequirement) {
            return false
        }
        
        if (bondLevelRequirement != null && currentBondLevel < bondLevelRequirement) {
            return false
        }
        
        if (energyRequirement != null && currentEnergy < energyRequirement) {
            return false
        }
        
        if (timeOfDayRequirement != null) {
            val currentTimeOfDay = getTimeOfDay(currentTime)
            if (currentTimeOfDay != timeOfDayRequirement) {
                return false
            }
        }
        
        return true
    }
    
    private fun getTimeOfDay(timestamp: Long): TimeOfDay {
        val hour = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
        }.get(java.util.Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> TimeOfDay.MORNING
            in 12..17 -> TimeOfDay.AFTERNOON
            in 18..22 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }
}

@Serializable
data class SpecialDialogueConfig(
    val dialogueId: String,
    val dialogue: String,
    val condition: SpecialDialogueCondition,
    val triggerOnce: Boolean = true,
    val triggered: Boolean = false,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class EffectConfig(
    val effectId: String,
    val effectType: InteractionEffectType,
    val duration: Long,
    val intensity: Float = 1.0f,
    val animationCurve: AnimationCurve = AnimationCurve.EASE_IN_OUT,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class PersonalityTrait(
    val traitId: String,
    val traitName: String,
    val description: String,
    val influenceOnDialogue: Float = 1.0f,
    val influenceOnEffects: Float = 1.0f
)

@Serializable
data class VoiceSettings(
    val enabled: Boolean = true,
    val volume: Float = 0.8f,
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f,
    val voiceType: VoiceType = VoiceType.NORMAL
)

@Serializable
data class AnimationSettings(
    val enabled: Boolean = true,
    val animationSpeed: Float = 1.0f,
    val enableParticles: Boolean = true,
    val enableShadows: Boolean = true,
    val particleCount: Int = 20
)

enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT
}

enum class SpecialDialogueCondition {
    FIRST_INTERACTION,
    LOW_ENERGY,
    HIGH_ENERGY,
    LOW_MOOD,
    HIGH_MOOD,
    BOND_LEVEL_UP,
    LEVEL_UP,
    ACHIEVEMENT_UNLOCK,
    SPECIAL_EVENT,
    BIRTHDAY,
    ANNIVERSARY
}

enum class AnimationCurve {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    BOUNCE,
    ELASTIC
}

enum class VoiceType {
    NORMAL,
    HAPPY,
    SAD,
    EXCITED,
    CALM,
    SLEEPY
}

data class CompanionDialogueState(
    val currentDialogue: String? = null,
    val dialogueHistory: List<DialogueEntry> = emptyList(),
    val lastDialogueTime: Long = 0L,
    val dialogueCooldowns: Map<String, Long> = emptyMap()
) {
    fun canShowDialogue(dialogueId: String, currentTime: Long, cooldown: Long): Boolean {
        val lastShownTime = dialogueCooldowns[dialogueId] ?: 0L
        return (currentTime - lastShownTime) >= cooldown
    }
    
    fun updateDialogueCooldown(dialogueId: String, currentTime: Long): CompanionDialogueState {
        val updatedCooldowns = dialogueCooldowns.toMutableMap()
        updatedCooldowns[dialogueId] = currentTime
        return copy(dialogueCooldowns = updatedCooldowns)
    }
    
    fun clearOldDialogues(maxAge: Long): CompanionDialogueState {
        val currentTime = System.currentTimeMillis()
        val recentHistory = dialogueHistory.filter { 
            (currentTime - it.timestamp) < maxAge 
        }
        return copy(dialogueHistory = recentHistory)
    }
}

data class CompanionEffectState(
    val activeEffects: List<InteractionEffect> = emptyList(),
    val effectHistory: List<InteractionEffect> = emptyList(),
    val lastEffectTime: Long = 0L
) {
    fun getUnexpiredActiveEffects(): List<InteractionEffect> {
        val currentTime = System.currentTimeMillis()
        return activeEffects.filter { it.endTime > currentTime }
    }
    
    fun addEffect(effect: InteractionEffect): CompanionEffectState {
        val updatedActiveEffects = activeEffects.toMutableList()
        updatedActiveEffects.add(effect)
        
        val updatedHistory = effectHistory.toMutableList()
        updatedHistory.add(effect)
        
        if (updatedHistory.size > 100) {
            updatedHistory.removeAt(0)
        }
        
        return copy(
            activeEffects = updatedActiveEffects,
            effectHistory = updatedHistory,
            lastEffectTime = System.currentTimeMillis()
        )
    }
    
    fun clearExpiredEffects(): CompanionEffectState {
        val currentTime = System.currentTimeMillis()
        val active = activeEffects.filter { it.endTime > currentTime }
        return copy(activeEffects = active)
    }
    
    fun clearOldHistory(maxAge: Long): CompanionEffectState {
        val currentTime = System.currentTimeMillis()
        val recentHistory = effectHistory.filter { 
            (currentTime - it.startTime) < maxAge 
        }
        return copy(effectHistory = recentHistory)
    }
}
