package com.maincharacter.shared.model

data class CompanionState(
    val equippedSkinId: String = "default",
    val availableSkins: List<String> = emptyList(),
    val currentEnergy: Int = 100,
    val maxEnergy: Int = 100,
    val isLowEnergy: Boolean = false,
    val currentMood: Int = 80,
    val maxMood: Int = 100,
    val companionName: String = "Companion",
    val companionLevel: Int = 1,
    val bondLevel: Int = 1,
    val maxBondLevel: Int = 100
) {
    fun getEnergyPercentage(): Float = currentEnergy.toFloat() / maxEnergy.toFloat()

    fun getMoodPercentage(): Float = currentMood.toFloat() / maxMood.toFloat()

    fun getBondPercentage(): Float = bondLevel.toFloat() / maxBondLevel.toFloat()

    fun isEnergyCritical(): Boolean = getEnergyPercentage() < 0.2f

    fun isEnergyWarning(): Boolean {
        val percentage = getEnergyPercentage()
        return percentage < 0.3f && percentage >= 0.2f
    }

    fun isMoodCritical(): Boolean = getMoodPercentage() < 0.2f
}
