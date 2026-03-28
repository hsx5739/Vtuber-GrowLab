package com.maincharacter.shared.service

import com.maincharacter.shared.model.CompanionConfig
import com.maincharacter.shared.model.CompanionState
import com.maincharacter.shared.model.DialogueEntry
import com.maincharacter.shared.model.InteractionEffect
import com.maincharacter.shared.model.InteractionEffectType
import com.maincharacter.shared.model.InteractionType
import com.maincharacter.shared.model.SwipeDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class CompanionInteractionService(
    private val companionConfig: CompanionConfig,
    private val companionState: StateFlow<CompanionState>
) {
    private val _currentDialogue = MutableStateFlow<String?>(null)
    val currentDialogue: StateFlow<String?> = _currentDialogue

    private val _dialogueHistory = MutableStateFlow<List<DialogueEntry>>(emptyList())
    val dialogueHistory: StateFlow<List<DialogueEntry>> = _dialogueHistory

    private val _interactionEffects = MutableStateFlow<List<InteractionEffect>>(emptyList())
    val interactionEffects: StateFlow<List<InteractionEffect>> = _interactionEffects

    private val _interactionCount = MutableStateFlow(0)
    val interactionCount: StateFlow<Int> = _interactionCount

    private val _lastInteractionTime = MutableStateFlow(0L)
    val lastInteractionTime: StateFlow<Long> = _lastInteractionTime

    fun handleInteraction(
        interactionType: InteractionType,
        context: InteractionContext
    ): InteractionResult {
        val state = companionState.value
        val dialogue = generateDialogue(interactionType, state, context)
        val effect = generateInteractionEffect(interactionType, state)
        recordInteraction(dialogue, effect, interactionType)
        return InteractionResult(
            success = true,
            dialogue = dialogue,
            effect = effect,
            interactionType = interactionType
        )
    }

    fun handleTapInteraction(): InteractionResult =
        handleInteraction(
            InteractionType.TAP,
            InteractionContext(
                energy = companionState.value.currentEnergy,
                mood = companionState.value.currentMood,
                bondLevel = companionState.value.bondLevel
            )
        )

    fun handleLongPressInteraction(): InteractionResult =
        handleInteraction(
            InteractionType.LONG_PRESS,
            InteractionContext(
                energy = companionState.value.currentEnergy,
                mood = companionState.value.currentMood,
                bondLevel = companionState.value.bondLevel
            )
        )

    fun handleDoubleTapInteraction(): InteractionResult =
        handleInteraction(
            InteractionType.DOUBLE_TAP,
            InteractionContext(
                energy = companionState.value.currentEnergy,
                mood = companionState.value.currentMood,
                bondLevel = companionState.value.bondLevel
            )
        )

    fun handleSwipeInteraction(direction: SwipeDirection): InteractionResult =
        handleInteraction(
            InteractionType.SWIPE,
            InteractionContext(
                energy = companionState.value.currentEnergy,
                mood = companionState.value.currentMood,
                bondLevel = companionState.value.bondLevel,
                swipeDirection = direction
            )
        )

    private fun generateDialogue(
        interactionType: InteractionType,
        state: CompanionState,
        context: InteractionContext
    ): String {
        val configDialogue = companionConfig
            .getDialogues(interactionType, state.currentMood, state.bondLevel)
            .randomOrNull()
        if (configDialogue != null) return configDialogue

        val energyTone = when {
            context.energy <= 20 -> "I'm low on energy."
            context.energy <= 50 -> "Be gentle with me."
            else -> "I'm here with you."
        }

        return when (interactionType) {
            InteractionType.TAP -> "${state.companionName}: $energyTone"
            InteractionType.LONG_PRESS -> "${state.companionName}: That feels reassuring."
            InteractionType.DOUBLE_TAP -> "${state.companionName}: You're being playful today."
            InteractionType.SWIPE -> "${state.companionName}: Smooth move."
        }
    }

    private fun generateInteractionEffect(
        interactionType: InteractionType,
        state: CompanionState
    ): InteractionEffect {
        val effectType = when (interactionType) {
            InteractionType.TAP -> InteractionEffectType.SHAKE
            InteractionType.LONG_PRESS -> InteractionEffectType.SCALE
            InteractionType.DOUBLE_TAP -> InteractionEffectType.ROTATE
            InteractionType.SWIPE -> InteractionEffectType.SLIDE
        }

        val duration = when (effectType) {
            InteractionEffectType.SHAKE -> 300L
            InteractionEffectType.SCALE -> 500L
            InteractionEffectType.ROTATE -> 400L
            InteractionEffectType.SLIDE -> 600L
            InteractionEffectType.FADE -> 300L
            InteractionEffectType.BOUNCE -> 450L
            InteractionEffectType.SPARKLE -> 350L
        }

        val intensity = when {
            state.isEnergyCritical() -> 0.5f
            state.isMoodCritical() -> 0.6f
            else -> 1.0f
        }

        val now = System.currentTimeMillis()
        return InteractionEffect(
            effectId = generateEffectId(),
            effectType = effectType,
            duration = duration,
            intensity = intensity,
            startTime = now,
            endTime = now + duration,
            metadata = mapOf("interaction_type" to interactionType.name)
        )
    }

    private fun recordInteraction(
        dialogue: String,
        effect: InteractionEffect,
        interactionType: InteractionType
    ) {
        _currentDialogue.value = dialogue

        val entry = DialogueEntry(
            id = generateDialogueId(),
            dialogue = dialogue,
            interactionType = interactionType,
            timestamp = System.currentTimeMillis()
        )
        _dialogueHistory.value = (_dialogueHistory.value + entry).takeLast(100)
        _interactionEffects.value = (_interactionEffects.value + effect).takeLast(20)
        _interactionCount.value = _interactionCount.value + 1
        _lastInteractionTime.value = System.currentTimeMillis()
        clearExpiredEffects()
    }

    private fun clearExpiredEffects() {
        val now = System.currentTimeMillis()
        _interactionEffects.value = _interactionEffects.value.filter { it.endTime > now }
    }

    fun getCurrentDialogue(): String? = _currentDialogue.value

    fun clearDialogue() {
        _currentDialogue.value = null
    }

    fun getDialogueHistory(limit: Int = 10): List<DialogueEntry> =
        _dialogueHistory.value.takeLast(limit)

    fun getActiveEffects(): List<InteractionEffect> {
        val now = System.currentTimeMillis()
        return _interactionEffects.value.filter { it.endTime > now }
    }

    fun getInteractionCount(): Int = _interactionCount.value

    fun getLastInteractionTime(): Long = _lastInteractionTime.value

    fun getTimeSinceLastInteraction(): Long =
        System.currentTimeMillis() - _lastInteractionTime.value

    fun getTimeSinceLastInteractionMinutes(): Float =
        getTimeSinceLastInteraction() / 60000f

    fun getInteractionStatistics(): InteractionStatistics {
        val history = _dialogueHistory.value
        val tapCount = history.count { it.interactionType == InteractionType.TAP }
        val longPressCount = history.count { it.interactionType == InteractionType.LONG_PRESS }
        val doubleTapCount = history.count { it.interactionType == InteractionType.DOUBLE_TAP }
        val swipeCount = history.count { it.interactionType == InteractionType.SWIPE }
        val mostFrequent = history
            .groupingBy { it.interactionType }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: InteractionType.TAP

        return InteractionStatistics(
            totalInteractions = history.size,
            tapCount = tapCount,
            longPressCount = longPressCount,
            doubleTapCount = doubleTapCount,
            swipeCount = swipeCount,
            mostFrequentInteraction = mostFrequent
        )
    }

    fun clearAllData() {
        _currentDialogue.value = null
        _dialogueHistory.value = emptyList()
        _interactionEffects.value = emptyList()
        _interactionCount.value = 0
        _lastInteractionTime.value = 0L
    }

    private fun generateDialogueId(): String = "dialogue_${System.currentTimeMillis()}_${(0..9999).random()}"

    private fun generateEffectId(): String = "effect_${System.currentTimeMillis()}_${(0..9999).random()}"
}

@Serializable
data class InteractionResult(
    val success: Boolean,
    val dialogue: String?,
    val effect: InteractionEffect,
    val interactionType: InteractionType
)

@Serializable
data class InteractionContext(
    val energy: Int,
    val mood: Int,
    val bondLevel: Int,
    val swipeDirection: SwipeDirection? = null
)

@Serializable
data class InteractionStatistics(
    val totalInteractions: Int,
    val tapCount: Int,
    val longPressCount: Int,
    val doubleTapCount: Int,
    val swipeCount: Int,
    val mostFrequentInteraction: InteractionType
)
