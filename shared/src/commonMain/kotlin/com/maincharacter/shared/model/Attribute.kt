package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Attribute(
    val key: StatKey,
    val value: Int,
    val minValue: Int = 0,
    val maxValue: Int = 100
) {
    fun clamp(): Attribute {
        return copy(value = value.coerceIn(minValue, maxValue))
    }
    
    fun add(delta: Int): Attribute {
        return copy(value = (value + delta).coerceIn(minValue, maxValue))
    }
    
    fun isMaxed(): Boolean = value >= maxValue
    fun isMin(): Boolean = value <= minValue
    fun getPercentage(): Float = value.toFloat() / maxValue.toFloat()
}

@Serializable
data class AttributeSnapshot(
    val fortune: Int,
    val vitality: Int,
    val mood: Int,
    val bond: Int,
    val focus: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AttributeChange(
    val key: StatKey,
    val oldValue: Int,
    val newValue: Int,
    val delta: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AttributeConstraints(
    val fortune: IntRange = 0..100,
    val vitality: IntRange = 0..100,
    val mood: IntRange = 0..100,
    val bond: IntRange = 0..100,
    val focus: IntRange = 0..100
) {
    fun getRange(key: StatKey): IntRange {
        return when (key) {
            StatKey.FORTUNE -> fortune
            StatKey.VITALITY -> vitality
            StatKey.MOOD -> mood
            StatKey.BOND -> bond
            StatKey.FOCUS -> focus
        }
    }
    
    fun clamp(key: StatKey, value: Int): Int {
        return value.coerceIn(getRange(key))
    }
}

object AttributeDefaults {
    const val FORTUNE_DEFAULT = 0
    const val VITALITY_DEFAULT = 50
    const val MOOD_DEFAULT = 50
    const val BOND_DEFAULT = 0
    const val FOCUS_DEFAULT = 0
    
    const val FORTUNE_MAX = 100
    const val VITALITY_MAX = 100
    const val MOOD_MAX = 100
    const val BOND_MAX = 100
    const val FOCUS_MAX = 100
    
    fun getDefaultValue(key: StatKey): Int {
        return when (key) {
            StatKey.FORTUNE -> FORTUNE_DEFAULT
            StatKey.VITALITY -> VITALITY_DEFAULT
            StatKey.MOOD -> MOOD_DEFAULT
            StatKey.BOND -> BOND_DEFAULT
            StatKey.FOCUS -> FOCUS_DEFAULT
        }
    }
    
    fun getMaxValue(key: StatKey): Int {
        return when (key) {
            StatKey.FORTUNE -> FORTUNE_MAX
            StatKey.VITALITY -> VITALITY_MAX
            StatKey.MOOD -> MOOD_MAX
            StatKey.BOND -> BOND_MAX
            StatKey.FOCUS -> FOCUS_MAX
        }
    }
}
