package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class AttributeType {
    FORTUNE,
    ENERGY,
    MOOD,
    BOND,
    FOCUS,
    VITALITY,
    EXPERIENCE
}

@Serializable
enum class InteractionType {
    TAP,
    LONG_PRESS,
    DOUBLE_TAP,
    SWIPE
}

@Serializable
enum class InteractionEffectType {
    SHAKE,
    SCALE,
    ROTATE,
    SLIDE,
    FADE,
    BOUNCE,
    SPARKLE
}

@Serializable
enum class SwipeDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

@Serializable
data class InteractionEffect(
    val effectId: String,
    val effectType: InteractionEffectType,
    val duration: Long,
    val intensity: Float,
    val startTime: Long,
    val endTime: Long,
    val metadata: Map<String, String> = emptyMap()
) {
    fun isActive(): Boolean = System.currentTimeMillis() < endTime

    fun getProgress(): Float {
        val elapsed = System.currentTimeMillis() - startTime
        return (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }

    fun getRemainingDuration(): Long =
        (endTime - System.currentTimeMillis()).coerceAtLeast(0L)

    fun getRemainingDurationMs(): Long = getRemainingDuration()

    fun getRemainingDurationSeconds(): Float = getRemainingDurationMs() / 1000f
}

@Serializable
data class DialogueEntry(
    val id: String,
    val dialogue: String,
    val interactionType: InteractionType,
    val timestamp: Long
) {
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }

    fun getTimeSinceDialogue(): Long = System.currentTimeMillis() - timestamp

    fun getTimeSinceDialogueMinutes(): Float = getTimeSinceDialogue() / 60000f
}
