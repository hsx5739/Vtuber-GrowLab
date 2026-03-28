package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class PassiveSkillManager(
    private val skillRegistry: SkillRegistry,
    private val eventRegistry: EventRegistry
) {
    
    private val _activePassiveSkills = MutableStateFlow<List<SkillInstance>>(emptyList())
    val activePassiveSkills: StateFlow<List<SkillInstance>> = _activePassiveSkills
    
    private val _passiveEffects = MutableStateFlow<Map<String, PassiveEffect>>(emptyMap())
    val passiveEffects: StateFlow<Map<String, PassiveEffect>> = _passiveEffects
    
    private val _weightModifiers = MutableStateFlow<Map<String, Float>>(emptyMap())
    val weightModifiers: StateFlow<Map<String, Float>> = _weightModifiers
    
    private val _probabilityModifiers = MutableStateFlow<Map<String, Float>>(emptyMap())
    val probabilityModifiers: StateFlow<Map<String, Float>> = _probabilityModifiers
    
    private val _passiveStats = MutableStateFlow(PassiveStats())
    val passiveStats: StateFlow<PassiveStats> = _passiveStats
    
    fun activatePassiveSkill(skillInstance: SkillInstance): ActivationResult {
        val skillDef = skillRegistry.getSkillById(skillInstance.skillId)
        if (skillDef == null) {
            return ActivationResult(
                success = false,
                error = "Skill not found"
            )
        }
        
        if (skillDef.type != SkillType.PASSIVE) {
            return ActivationResult(
                success = false,
                error = "Skill is not passive"
            )
        }
        
        if (!skillInstance.isUnlocked) {
            return ActivationResult(
                success = false,
                error = "Skill is not unlocked"
            )
        }
        
        val effect = calculatePassiveEffect(skillDef, skillInstance)
        
        val updatedInstance = skillInstance.copy(
            isEquipped = true
        )
        
        val currentActive = _activePassiveSkills.value.toMutableList()
        currentActive.add(updatedInstance)
        _activePassiveSkills.value = currentActive
        
        val updatedEffects = _passiveEffects.value.toMutableMap()
        updatedEffects[skillInstance.skillId] = effect
        _passiveEffects.value = updatedEffects
        
        applyWeightModifiers(skillDef, effect)
        applyProbabilityModifiers(skillDef, effect)
        
        updateStats(ActivationType.ACTIVATE, skillDef)
        
        return ActivationResult(
            success = true,
            error = null
        )
    }
    
    fun deactivatePassiveSkill(skillId: String): DeactivationResult {
        val currentActive = _activePassiveSkills.value
        val skillInstance = currentActive.find { it.skillId == skillId }
        
        if (skillInstance == null) {
            return DeactivationResult(
                success = false,
                error = "Skill is not active"
            )
        }
        
        val skillDef = skillRegistry.getSkillById(skillId)
        if (skillDef == null) {
            return DeactivationResult(
                success = false,
                error = "Skill not found"
            )
        }
        
        val updatedActive = currentActive.filter { it.skillId != skillId }
        _activePassiveSkills.value = updatedActive
        
        val updatedEffects = _passiveEffects.value.toMutableMap()
        updatedEffects.remove(skillId)
        _passiveEffects.value = updatedEffects
        
        removeWeightModifiers(skillDef)
        removeProbabilityModifiers(skillDef)
        
        updateStats(ActivationType.DEACTIVATE, skillDef)
        
        return DeactivationResult(
            success = true,
            error = null
        )
    }
    
    private fun calculatePassiveEffect(
        skillDef: SkillDef,
        skillInstance: SkillInstance
    ): PassiveEffect {
        val weightModifiers = mutableMapOf<String, Float>()
        val probabilityModifiers = mutableMapOf<String, Float>()
        val costModifiers = mutableMapOf<String, Float>()
        val attributeModifiers = mutableMapOf<StatKey, Int>()
        val currencyModifiers = mutableMapOf<CurrencyType, Float>()
        
        skillDef.effects.forEach { effect ->
            when (effect.type) {
                SkillEffectType.WEIGHT_MODIFIER -> {
                    val targetEventIds = getTargetEventIds(effect)
                    targetEventIds.forEach { eventId ->
                        weightModifiers[eventId] = (weightModifiers[eventId] ?: 1.0f) * effect.value
                    }
                }
                SkillEffectType.PROBABILITY_BOOST -> {
                    val targetEventIds = getTargetEventIds(effect)
                    targetEventIds.forEach { eventId ->
                        probabilityModifiers[eventId] = (probabilityModifiers[eventId] ?: 1.0f) * effect.value
                    }
                }
                SkillEffectType.COST_REDUCTION -> {
                    val targetEventIds = getTargetEventIds(effect)
                    targetEventIds.forEach { eventId ->
                        costModifiers[eventId] = (costModifiers[eventId] ?: 1.0f) * (1 - effect.value)
                    }
                }
                SkillEffectType.ATTRIBUTE_BOOST -> {
                    val statKey = getTargetStatKey(effect)
                    if (statKey != null) {
                        attributeModifiers[statKey] = (attributeModifiers[statKey] ?: 0) + effect.value.toInt()
                    }
                }
                SkillEffectType.CURRENCY_BONUS -> {
                    val currency = getTargetCurrency(effect)
                    if (currency != null) {
                        currencyModifiers[currency] = (currencyModifiers[currency] ?: 1.0f) * effect.value
                    }
                }
                else -> {}
            }
        }
        
        return PassiveEffect(
            skillId = skillDef.id,
            skillName = skillDef.name,
            skillLevel = skillInstance.level,
            weightModifiers = weightModifiers,
            probabilityModifiers = probabilityModifiers,
            costModifiers = costModifiers,
            attributeModifiers = attributeModifiers,
            currencyModifiers = currencyModifiers,
            activationTime = System.currentTimeMillis(),
            duration = skillDef.effects.firstOrNull()?.duration ?: 0L
        )
    }
    
    private fun getTargetEventIds(effect: SkillEffect): List<String> {
        val target = effect.target
        
        return when (target) {
            EffectTarget.GLOBAL -> eventRegistry.getAllEvents().map { it.id }
            EffectTarget.EVENT -> {
                val eventId = effect.metadata["eventId"]
                if (eventId != null) {
                    listOf(eventId)
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
    
    private fun getTargetStatKey(effect: SkillEffect): StatKey? {
        val statKeyStr = effect.metadata["statKey"]
        return if (statKeyStr != null) {
            try {
                StatKey.valueOf(statKeyStr)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    private fun getTargetCurrency(effect: SkillEffect): CurrencyType? {
        val currencyStr = effect.metadata["currency"]
        return if (currencyStr != null) {
            try {
                CurrencyType.valueOf(currencyStr)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    private fun applyWeightModifiers(skillDef: SkillDef, effect: PassiveEffect) {
        val currentModifiers = _weightModifiers.value.toMutableMap()
        
        effect.weightModifiers.forEach { (eventId, modifier) ->
            currentModifiers[eventId] = (currentModifiers[eventId] ?: 1.0f) * modifier
        }
        
        _weightModifiers.value = currentModifiers
    }
    
    private fun applyProbabilityModifiers(skillDef: SkillDef, effect: PassiveEffect) {
        val currentModifiers = _probabilityModifiers.value.toMutableMap()
        
        effect.probabilityModifiers.forEach { (eventId, modifier) ->
            currentModifiers[eventId] = (currentModifiers[eventId] ?: 1.0f) * modifier
        }
        
        _probabilityModifiers.value = currentModifiers
    }
    
    private fun removeWeightModifiers(skillDef: SkillDef) {
        val currentModifiers = _weightModifiers.value.toMutableMap()
        val effect = _passiveEffects.value[skillDef.id] ?: return
        
        effect.weightModifiers.forEach { (eventId, modifier) ->
            val current = currentModifiers[eventId] ?: return@forEach
            currentModifiers[eventId] = current / modifier
        }
        
        _weightModifiers.value = currentModifiers
    }
    
    private fun removeProbabilityModifiers(skillDef: SkillDef) {
        val currentModifiers = _probabilityModifiers.value.toMutableMap()
        val effect = _passiveEffects.value[skillDef.id] ?: return
        
        effect.probabilityModifiers.forEach { (eventId, modifier) ->
            val current = currentModifiers[eventId] ?: return@forEach
            currentModifiers[eventId] = current / modifier
        }
        
        _probabilityModifiers.value = currentModifiers
    }
    
    fun getWeightModifier(eventId: String): Float {
        return _weightModifiers.value[eventId] ?: 1.0f
    }
    
    fun getProbabilityModifier(eventId: String): Float {
        return _probabilityModifiers.value[eventId] ?: 1.0f
    }
    
    fun getAllWeightModifiers(): Map<String, Float> {
        return _weightModifiers.value
    }
    
    fun getAllProbabilityModifiers(): Map<String, Float> {
        return _probabilityModifiers.value
    }
    
    fun getPassiveEffect(skillId: String): PassiveEffect? {
        return _passiveEffects.value[skillId]
    }
    
    fun getAllPassiveEffects(): Map<String, PassiveEffect> {
        return _passiveEffects.value
    }
    
    fun getActivePassiveSkills(): List<SkillInstance> {
        return _activePassiveSkills.value
    }
    
    fun getPassiveSkillsByCategory(category: SkillCategory): List<SkillInstance> {
        return _activePassiveSkills.value.filter { instance ->
            skillRegistry.getSkillById(instance.skillId)?.category == category
        }
    }
    
    fun getPassiveSkillsByRarity(rarity: SkillRarity): List<SkillInstance> {
        return _activePassiveSkills.value.filter { instance ->
            skillRegistry.getSkillById(instance.skillId)?.rarity == rarity
        }
    }
    
    fun getPassiveSkillsByLevel(minLevel: Int, maxLevel: Int): List<SkillInstance> {
        return _activePassiveSkills.value.filter { instance ->
            instance.level >= minLevel && instance.level <= maxLevel
        }
    }
    
    fun getPassiveSkillCount(): Int {
        return _activePassiveSkills.value.size
    }
    
    fun getMaxPassiveSkills(): Int {
        return 3
    }
    
    fun canActivateMore(): Boolean {
        return getPassiveSkillCount() < getMaxPassiveSkills()
    }
    
    fun getAvailableSlots(): Int {
        return getMaxPassiveSkills() - getPassiveSkillCount()
    }
    
    fun getPassiveSkillSummary(): PassiveSkillSummary {
        val activeSkills = _activePassiveSkills.value
        val effects = _passiveEffects.value
        
        val totalWeightModifiers = effects.values.sumOf { 
            it.weightModifiers.size 
        }
        val totalProbabilityModifiers = effects.values.sumOf { 
            it.probabilityModifiers.size 
        }
        val totalCostModifiers = effects.values.sumOf { 
            it.costModifiers.size 
        }
        val totalAttributeModifiers = effects.values.sumOf { 
            it.attributeModifiers.size 
        }
        val totalCurrencyModifiers = effects.values.sumOf { 
            it.currencyModifiers.size 
        }
        
        val averageLevel = if (activeSkills.isNotEmpty()) {
            activeSkills.map { it.level }.average().toFloat()
        } else {
            0f
        }
        
        return PassiveSkillSummary(
            activeSkillCount = activeSkills.size,
            maxSkillCount = getMaxPassiveSkills(),
            availableSlots = getAvailableSlots(),
            totalWeightModifiers = totalWeightModifiers,
            totalProbabilityModifiers = totalProbabilityModifiers,
            totalCostModifiers = totalCostModifiers,
            totalAttributeModifiers = totalAttributeModifiers,
            totalCurrencyModifiers = totalCurrencyModifiers,
            averageLevel = averageLevel,
            highestLevel = activeSkills.maxByOrNull { it.level }?.level ?: 0,
            lowestLevel = activeSkills.minByOrNull { it.level }?.level ?: 0
        )
    }
    
    fun getPassiveSkillStats(): PassiveStats {
        return _passiveStats.value
    }
    
    fun clearAllPassiveSkills() {
        _activePassiveSkills.value = emptyList()
        _passiveEffects.value = emptyMap()
        _weightModifiers.value = emptyMap()
        _probabilityModifiers.value = emptyMap()
    }
    
    private fun updateStats(type: ActivationType, skillDef: SkillDef) {
        val current = _passiveStats.value
        
        val updated = when (type) {
            ActivationType.ACTIVATE -> current.copy(
                totalActivations = current.totalActivations + 1,
                skillActivations = current.skillActivations.toMutableMap().apply {
                    this[skillDef.id] = (this[skillDef.id] ?: 0) + 1
                },
                categoryActivations = current.categoryActivations.toMutableMap().apply {
                    this[skillDef.category] = (this[skillDef.category] ?: 0) + 1
                }
            )
            ActivationType.DEACTIVATE -> current.copy(
                totalDeactivations = current.totalDeactivations + 1,
                skillDeactivations = current.skillDeactivations.toMutableMap().apply {
                    this[skillDef.id] = (this[skillDef.id] ?: 0) + 1
                }
            )
        }
        
        _passiveStats.value = updated
    }
    
    fun getSkillActivationCount(skillId: String): Int {
        return _passiveStats.value.skillActivations[skillId] ?: 0
    }
    
    fun getSkillDeactivationCount(skillId: String): Int {
        return _passiveStats.value.skillDeactivations[skillId] ?: 0
    }
    
    fun getCategoryActivationCount(category: SkillCategory): Int {
        return _passiveStats.value.categoryActivations[category] ?: 0
    }
    
    fun getMostActivatedSkill(): String? {
        return _passiveStats.value.skillActivations.maxByOrNull { it.value }?.key
    }
    
    fun getMostActivatedCategory(): SkillCategory? {
        return _passiveStats.value.categoryActivations.maxByOrNull { it.value }?.key
    }
}

@Serializable
data class PassiveEffect(
    val skillId: String,
    val skillName: String,
    val skillLevel: Int,
    val weightModifiers: Map<String, Float>,
    val probabilityModifiers: Map<String, Float>,
    val costModifiers: Map<String, Float>,
    val attributeModifiers: Map<StatKey, Int>,
    val currencyModifiers: Map<CurrencyType, Float>,
    val activationTime: Long,
    val duration: Long
) {
    fun getWeightModifier(eventId: String): Float {
        return weightModifiers[eventId] ?: 1.0f
    }
    
    fun getProbabilityModifier(eventId: String): Float {
        return probabilityModifiers[eventId] ?: 1.0f
    }
    
    fun getCostModifier(eventId: String): Float {
        return costModifiers[eventId] ?: 1.0f
    }
    
    fun getAttributeModifier(statKey: StatKey): Int {
        return attributeModifiers[statKey] ?: 0
    }
    
    fun getCurrencyModifier(currency: CurrencyType): Float {
        return currencyModifiers[currency] ?: 1.0f
    }
    
    fun getTotalWeightModifiers(): Int {
        return weightModifiers.size
    }
    
    fun getTotalProbabilityModifiers(): Int {
        return probabilityModifiers.size
    }
    
    fun getTotalCostModifiers(): Int {
        return costModifiers.size
    }
    
    fun getTotalAttributeModifiers(): Int {
        return attributeModifiers.size
    }
    
    fun getTotalCurrencyModifiers(): Int {
        return currencyModifiers.size
    }
    
    fun isActive(): Boolean {
        if (duration == 0L) return true
        
        val elapsed = System.currentTimeMillis() - activationTime
        return elapsed < duration
    }
    
    fun getRemainingDuration(): Long {
        if (duration == 0L) return Long.MAX_VALUE
        
        val elapsed = System.currentTimeMillis() - activationTime
        return (duration - elapsed).coerceAtLeast(0L)
    }
}

@Serializable
data class ActivationResult(
    val success: Boolean,
    val error: String?
)

@Serializable
data class DeactivationResult(
    val success: Boolean,
    val error: String?
)

@Serializable
data class PassiveSkillSummary(
    val activeSkillCount: Int,
    val maxSkillCount: Int,
    val availableSlots: Int,
    val totalWeightModifiers: Int,
    val totalProbabilityModifiers: Int,
    val totalCostModifiers: Int,
    val totalAttributeModifiers: Int,
    val totalCurrencyModifiers: Int,
    val averageLevel: Float,
    val highestLevel: Int,
    val lowestLevel: Int
) {
    fun getUsageRate(): Float {
        return if (maxSkillCount > 0) {
            (activeSkillCount.toFloat() / maxSkillCount.toFloat()) * 100f
        } else {
            0f
        }
    }
}

@Serializable
data class PassiveStats(
    val totalActivations: Int = 0,
    val totalDeactivations: Int = 0,
    val skillActivations: Map<String, Int> = emptyMap(),
    val skillDeactivations: Map<String, Int> = emptyMap(),
    val categoryActivations: Map<SkillCategory, Int> = emptyMap()
) {
    fun getActivationRate(): Float {
        return if (totalActivations > 0) {
            (totalActivations.toFloat() / (totalActivations + totalDeactivations).toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getMostActivatedSkill(): String? {
        return skillActivations.maxByOrNull { it.value }?.key
    }
    
    fun getMostActivatedCategory(): SkillCategory? {
        return categoryActivations.maxByOrNull { it.value }?.key
    }
}

enum class ActivationType {
    ACTIVATE,
    DEACTIVATE
}
