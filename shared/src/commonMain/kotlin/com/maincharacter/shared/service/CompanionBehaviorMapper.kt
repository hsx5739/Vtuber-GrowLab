package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CompanionBehaviorMapper {
    
    private val _behaviorState = MutableStateFlow(CompanionBehaviorState())
    val behaviorState: StateFlow<CompanionBehaviorState> = _behaviorState
    
    fun mapAttributesToBehavior(state: HostState): CompanionBehavior {
        val fortune = state.fortune
        val vitality = state.vitality
        val mood = state.mood
        val bond = state.bond
        val focus = state.focus
        
        val moodState = mapMoodToState(mood)
        val energyLevel = mapVitalityToEnergy(vitality)
        val intimacyLevel = mapBondToIntimacy(bond)
        val focusState = mapFocusToState(focus)
        
        return CompanionBehavior(
            moodState = moodState,
            energyLevel = energyLevel,
            intimacyLevel = intimacyLevel,
            focusState = focusState,
            idleVariant = determineIdleVariant(mood, bond),
            dialogueTone = determineDialogueTone(mood, bond),
            interactionStyle = determineInteractionStyle(focus, bond),
            responseSpeed = determineResponseSpeed(focus, vitality)
        )
    }
    
    private fun mapMoodToState(mood: Int): MoodState {
        return when {
            mood >= 80 -> MoodState.HAPPY
            mood >= 60 -> MoodState.GOOD
            mood >= 40 -> MoodState.NORMAL
            mood >= 20 -> MoodState.SAD
            else -> MoodState.DEJECTED
        }
    }
    
    private fun mapVitalityToEnergy(vitality: Int): EnergyLevel {
        return when {
            vitality >= 80 -> EnergyLevel.HIGH
            vitality >= 60 -> EnergyLevel.MEDIUM_HIGH
            vitality >= 40 -> EnergyLevel.MEDIUM
            vitality >= 20 -> EnergyLevel.LOW
            else -> EnergyLevel.VERY_LOW
        }
    }
    
    private fun mapBondToIntimacy(bond: Int): IntimacyLevel {
        return when {
            bond >= 90 -> IntimacyLevel.MAXIMUM
            bond >= 70 -> IntimacyLevel.HIGH
            bond >= 50 -> IntimacyLevel.MEDIUM
            bond >= 30 -> IntimacyLevel.LOW
            bond >= 10 -> IntimacyLevel.ACQUAINTANCE
            else -> IntimacyLevel.STRANGER
        }
    }
    
    private fun mapFocusToState(focus: Int): FocusState {
        return when {
            focus >= 80 -> FocusState.SHARP
            focus >= 60 -> FocusState.CLEAR
            focus >= 40 -> FocusState.NORMAL
            focus >= 20 -> FocusState.DISTRACTED
            else -> FocusState.ABSENT
        }
    }
    
    private fun determineIdleVariant(mood: Int, bond: Int): String {
        return when {
            mood >= 80 && bond >= 70 -> "idle_happy_close"
            mood >= 80 -> "idle_happy"
            bond >= 70 -> "idle_close"
            mood <= 20 -> "idle_sad"
            else -> "idle_normal"
        }
    }
    
    private fun determineDialogueTone(mood: Int, bond: Int): DialogueTone {
        return when {
            mood >= 80 -> DialogueTone.CHEERFUL
            mood >= 60 -> DialogueTone.FRIENDLY
            mood >= 40 -> DialogueTone.NEUTRAL
            mood >= 20 -> DialogueTone.CONCERNED
            else -> DialogueTone.WORRIED
        }
    }
    
    private fun determineInteractionStyle(focus: Int, bond: Int): InteractionStyle {
        return when {
            focus >= 70 && bond >= 70 -> InteractionStyle.ATTENTIVE
            focus >= 70 -> InteractionStyle.FOCUSED
            bond >= 70 -> InteractionStyle.AFFECTIONATE
            else -> InteractionStyle.CASUAL
        }
    }
    
    private fun determineResponseSpeed(focus: Int, vitality: Int): ResponseSpeed {
        val avg = (focus + vitality) / 2
        return when {
            avg >= 80 -> ResponseSpeed.INSTANT
            avg >= 60 -> ResponseSpeed.QUICK
            avg >= 40 -> ResponseSpeed.NORMAL
            avg >= 20 -> ResponseSpeed.SLOW
            else -> ResponseSpeed.VERY_SLOW
        }
    }
    
    fun getBehaviorDescription(behavior: CompanionBehavior): String {
        val moodDesc = when (behavior.moodState) {
            MoodState.HAPPY -> "心情愉悦"
            MoodState.GOOD -> "心情不错"
            MoodState.NORMAL -> "心情平静"
            MoodState.SAD -> "有些低落"
            MoodState.DEJECTED -> "非常沮丧"
        }
        
        val energyDesc = when (behavior.energyLevel) {
            EnergyLevel.HIGH -> "精力充沛"
            EnergyLevel.MEDIUM_HIGH -> "精力较好"
            EnergyLevel.MEDIUM -> "精力一般"
            EnergyLevel.LOW -> "有些疲惫"
            EnergyLevel.VERY_LOW -> "非常疲惫"
        }
        
        val intimacyDesc = when (behavior.intimacyLevel) {
            IntimacyLevel.MAXIMUM -> "亲密无间"
            IntimacyLevel.HIGH -> "关系很好"
            IntimacyLevel.MEDIUM -> "关系一般"
            IntimacyLevel.LOW -> "有些疏远"
            IntimacyLevel.ACQUAINTANCE -> "刚刚认识"
            IntimacyLevel.STRANGER -> "陌生人"
        }
        
        return "当前状态：$moodDesc，$energyDesc，$intimacyDesc"
    }
    
    fun updateBehaviorState(state: HostState) {
        val behavior = mapAttributesToBehavior(state)
        _behaviorState.value = CompanionBehaviorState(
            behavior = behavior,
            timestamp = System.currentTimeMillis()
        )
    }
}

data class CompanionBehavior(
    val moodState: MoodState,
    val energyLevel: EnergyLevel,
    val intimacyLevel: IntimacyLevel,
    val focusState: FocusState,
    val idleVariant: String,
    val dialogueTone: DialogueTone,
    val interactionStyle: InteractionStyle,
    val responseSpeed: ResponseSpeed
)

data class CompanionBehaviorState(
    val behavior: CompanionBehavior? = null,
    val timestamp: Long = 0
)

enum class MoodState {
    HAPPY,
    GOOD,
    NORMAL,
    SAD,
    DEJECTED
}

enum class EnergyLevel {
    HIGH,
    MEDIUM_HIGH,
    MEDIUM,
    LOW,
    VERY_LOW
}

enum class IntimacyLevel {
    STRANGER,
    ACQUAINTANCE,
    LOW,
    MEDIUM,
    HIGH,
    MAXIMUM
}

enum class FocusState {
    SHARP,
    CLEAR,
    NORMAL,
    DISTRACTED,
    ABSENT
}

enum class DialogueTone {
    CHEERFUL,
    FRIENDLY,
    NEUTRAL,
    CONCERNED,
    WORRIED
}

enum class InteractionStyle {
    ATTENTIVE,
    FOCUSED,
    AFFECTIONATE,
    CASUAL
}

enum class ResponseSpeed {
    INSTANT,
    QUICK,
    NORMAL,
    SLOW,
    VERY_SLOW
}