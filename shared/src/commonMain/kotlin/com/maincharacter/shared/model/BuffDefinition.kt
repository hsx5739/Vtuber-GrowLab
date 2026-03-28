package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class BuffDefinition(
    val buffId: String,
    val buffName: String,
    val buffDescription: String,
    val buffType: BuffType,
    val buffCategory: BuffCategory,
    val duration: Long?,
    val maxDuration: Long?,
    val stackStrategy: BuffStackStrategy,
    val maxStacks: Int?,
    val refreshStrategy: BuffRefreshStrategy,
    val modifiers: List<BuffModifier>,
    val iconPath: String,
    val priority: Int,
    val isDispellable: Boolean,
    val isHidden: Boolean,
    val requirements: BuffRequirements,
    val conflicts: List<String>,
    val metadata: Map<String, String> = emptyMap()
) {
    fun hasDuration(): Boolean {
        return duration != null
    }
    
    fun hasMaxDuration(): Boolean {
        return maxDuration != null
    }
    
    fun isPermanent(): Boolean {
        return duration == null
    }
    
    fun canStack(): Boolean {
        return stackStrategy != BuffStackStrategy.NO_STACK
    }
    
    fun hasMaxStacks(): Boolean {
        return maxStacks != null
    }
    
    fun hasModifiers(): Boolean {
        return modifiers.isNotEmpty()
    }
    
    fun hasConflicts(): Boolean {
        return conflicts.isNotEmpty()
    }
    
    fun hasRequirements(): Boolean {
        return requirements.hasRequirements()
    }
    
    fun canBeDispelled(): Boolean {
        return isDispellable
    }
    
    fun isVisible(): Boolean {
        return !isHidden
    }
    
    fun getDurationMinutes(): Float? {
        return duration?.let { it / 60000f }
    }
    
    fun getMaxDurationMinutes(): Float? {
        return maxDuration?.let { it / 60000f }
    }
    
    fun getFormattedDuration(): String? {
        val minutes = getDurationMinutes() ?: return null
        return if (minutes < 1) {
            "${(minutes * 60).toInt()}s"
        } else if (minutes < 60) {
            "${String.format("%.0f", minutes)}m"
        } else {
            val hours = minutes / 60
            val mins = minutes % 60
            "${String.format("%.0f", hours)}h ${String.format("%.0f", mins)}m"
        }
    }
    
    fun getBuffTypeDisplayName(): String {
        return when (buffType) {
            BuffType.POSITIVE -> "Positive"
            BuffType.NEGATIVE -> "Negative"
            BuffType.NEUTRAL -> "Neutral"
        }
    }
    
    fun getBuffCategoryDisplayName(): String {
        return when (buffCategory) {
            BuffCategory.ATTRIBUTE -> "Attribute"
            BuffCategory.CURRENCY -> "Currency"
            BuffCategory.EXPERIENCE -> "Experience"
            BuffCategory.SKILL -> "Skill"
            BuffCategory.MOVEMENT -> "Movement"
            BuffCategory.COMBAT -> "Combat"
            BuffCategory.RESOURCE -> "Resource"
            BuffCategory.CUSTOM -> "Custom"
        }
    }
    
    fun getStackStrategyDisplayName(): String {
        return when (stackStrategy) {
            BuffStackStrategy.NO_STACK -> "No Stack"
            BuffStackStrategy.REFRESH_DURATION -> "Refresh Duration"
            BuffStackStrategy.EXTEND_DURATION -> "Extend Duration"
            BuffStackStrategy.STACK_INTENSITY -> "Stack Intensity"
            BuffStackStrategy.STACK_INTENSITY_MAX_3 -> "Stack Intensity (Max 3)"
        }
    }
    
    fun getRefreshStrategyDisplayName(): String {
        return when (refreshStrategy) {
            BuffRefreshStrategy.REFRESH_ON_APPLY -> "Refresh on Apply"
            BuffRefreshStrategy.REFRESH_ON_EXPIRE -> "Refresh on Expire"
            BuffRefreshStrategy.NO_REFRESH -> "No Refresh"
        }
    }
    
    fun getTotalAttributeMultiplier(): Float {
        return modifiers
            .filter { it.type == ModifierType.ATTRIBUTE_MULTIPLIER }
            .sumOf { it.value.toDouble() }
            .toFloat()
    }
    
    fun getTotalAttributeBonus(): Int {
        return modifiers
            .filter { it.type == ModifierType.ATTRIBUTE_BONUS }
            .sumOf { it.value.toInt() }
    }
    
    fun getTotalCurrencyMultiplier(): Float {
        return modifiers
            .filter { it.type == ModifierType.CURRENCY_MULTIPLIER }
            .sumOf { it.value.toDouble() }
            .toFloat()
    }
    
    fun getTotalCurrencyBonus(): Int {
        return modifiers
            .filter { it.type == ModifierType.CURRENCY_BONUS }
            .sumOf { it.value.toInt() }
    }
}

@Serializable
data class BuffState(
    val buffId: String,
    val userId: String,
    val source: BuffSource,
    val sourceId: String?,
    val startTime: Long,
    val endTime: Long?,
    val currentDuration: Long,
    val stackCount: Int,
    val currentModifiers: List<BuffModifier>,
    val isActive: Boolean,
    val isPaused: Boolean,
    val metadata: Map<String, String> = emptyMap()
) {
    fun isPermanent(): Boolean {
        return endTime == null
    }
    
    fun isExpired(): Boolean {
        val endTime = endTime ?: return false
        return System.currentTimeMillis() > endTime
    }
    
    fun getRemainingDuration(): Long? {
        val endTime = endTime ?: return null
        val currentTime = System.currentTimeMillis()
        return (endTime - currentTime).coerceAtLeast(0L)
    }
    
    fun getRemainingDurationMinutes(): Float? {
        return getRemainingDuration()?.let { it / 60000f }
    }
    
    fun getFormattedRemainingDuration(): String? {
        val minutes = getRemainingDurationMinutes() ?: return null
        return if (minutes < 1) {
            "${(minutes * 60).toInt()}s"
        } else if (minutes < 60) {
            "${String.format("%.0f", minutes)}m"
        } else {
            val hours = minutes / 60
            val mins = minutes % 60
            "${String.format("%.0f", hours)}h ${String.format("%.0f", mins)}m"
        }
    }
    
    fun getElapsedDuration(): Long {
        val currentTime = System.currentTimeMillis()
        return currentTime - startTime
    }
    
    fun getElapsedDurationMinutes(): Float {
        return getElapsedDuration() / 60000f
    }
    
    fun getFormattedElapsedDuration(): String {
        val minutes = getElapsedDurationMinutes()
        return if (minutes < 1) {
            "${(minutes * 60).toInt()}s"
        } else if (minutes < 60) {
            "${String.format("%.0f", minutes)}m"
        } else {
            val hours = minutes / 60
            val mins = minutes % 60
            "${String.format("%.0f", hours)}h ${String.format("%.0f", mins)}m"
        }
    }
    
    fun getFormattedStartTime(): String {
        val date = java.util.Date(startTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getFormattedEndTime(): String? {
        val timestamp = endTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun isAtMaxStacks(maxStacks: Int): Boolean {
        return stackCount >= maxStacks
    }
    
    fun canStack(maxStacks: Int): Boolean {
        return stackCount < maxStacks
    }
    
    fun getStackProgress(maxStacks: Int): Float {
        return stackCount.toFloat() / maxStacks.toFloat()
    }
    
    fun getStackProgressPercentage(maxStacks: Int): Int {
        return (getStackProgress(maxStacks) * 100).toInt()
    }
}

@Serializable
data class BuffModifier(
    val type: ModifierType,
    val target: ModifierTarget,
    val value: Float,
    val attributeType: AttributeType?,
    val currencyType: CurrencyType?,
    val skillType: String?,
    val description: String?
) {
    fun isAttributeModifier(): Boolean {
        return type == ModifierType.ATTRIBUTE_MULTIPLIER || type == ModifierType.ATTRIBUTE_BONUS
    }
    
    fun isCurrencyModifier(): Boolean {
        return type == ModifierType.CURRENCY_MULTIPLIER || type == ModifierType.CURRENCY_BONUS
    }
    
    fun isSkillModifier(): Boolean {
        return type == ModifierType.SKILL_COOLDOWN || type == ModifierType.SKILL_DAMAGE || type == ModifierType.SKILL_DURATION
    }
    
    fun isMovementModifier(): Boolean {
        return type == ModifierType.MOVEMENT_SPEED || type == ModifierType.MOVEMENT_RANGE
    }
    
    fun isCombatModifier(): Boolean {
        return type == ModifierType.COMBAT_DAMAGE || type == ModifierType.COMBAT_DEFENSE || type == ModifierType.COMBAT_SPEED
    }
    
    fun getModifierDescription(): String {
        return description ?: getDefaultDescription()
    }
    
    private fun getDefaultDescription(): String {
        val valueText = if (value > 0) "+${String.format("%.2f", value)}" else String.format("%.2f", value)
        
        return when (type) {
            ModifierType.ATTRIBUTE_MULTIPLIER -> "Attribute Multiplier: $valueText"
            ModifierType.ATTRIBUTE_BONUS -> "Attribute Bonus: $valueText"
            ModifierType.CURRENCY_MULTIPLIER -> "Currency Multiplier: $valueText"
            ModifierType.CURRENCY_BONUS -> "Currency Bonus: $valueText"
            ModifierType.SKILL_COOLDOWN -> "Skill Cooldown: $valueText"
            ModifierType.SKILL_DAMAGE -> "Skill Damage: $valueText"
            ModifierType.SKILL_DURATION -> "Skill Duration: $valueText"
            ModifierType.MOVEMENT_SPEED -> "Movement Speed: $valueText"
            ModifierType.MOVEMENT_RANGE -> "Movement Range: $valueText"
            ModifierType.COMBAT_DAMAGE -> "Combat Damage: $valueText"
            ModifierType.COMBAT_DEFENSE -> "Combat Defense: $valueText"
            ModifierType.COMBAT_SPEED -> "Combat Speed: $valueText"
            ModifierType.CUSTOM -> "Custom: $valueText"
        }
    }
}

@Serializable
data class BuffRequirements(
    val minLevel: Int?,
    val maxLevel: Int?,
    val requiredAttributes: Map<AttributeType, Int>,
    val requiredItems: List<String>,
    val requiredAchievements: List<String>,
    val requiredQuests: List<String>,
    val requiredBuffs: List<String>,
    val forbiddenBuffs: List<String>
) {
    fun hasLevelRequirement(): Boolean {
        return minLevel != null || maxLevel != null
    }
    
    fun hasAttributeRequirements(): Boolean {
        return requiredAttributes.isNotEmpty()
    }
    
    fun hasItemRequirements(): Boolean {
        return requiredItems.isNotEmpty()
    }
    
    fun hasAchievementRequirements(): Boolean {
        return requiredAchievements.isNotEmpty()
    }
    
    fun hasQuestRequirements(): Boolean {
        return requiredQuests.isNotEmpty()
    }
    
    fun hasBuffRequirements(): Boolean {
        return requiredBuffs.isNotEmpty()
    }
    
    fun hasForbiddenBuffs(): Boolean {
        return forbiddenBuffs.isNotEmpty()
    }
    
    fun hasRequirements(): Boolean {
        return hasLevelRequirement() ||
               hasAttributeRequirements() ||
               hasItemRequirements() ||
               hasAchievementRequirements() ||
               hasQuestRequirements() ||
               hasBuffRequirements() ||
               hasForbiddenBuffs()
    }
    
    fun meetsRequirements(
        userLevel: Int,
        userAttributes: Map<AttributeType, Int>,
        userItems: List<String>,
        completedAchievements: List<String>,
        completedQuests: List<String>,
        activeBuffs: List<String>
    ): Boolean {
        minLevel?.let { level ->
            if (userLevel < level) return false
        }
        
        maxLevel?.let { level ->
            if (userLevel > level) return false
        }
        
        requiredAttributes.forEach { (attribute, value) ->
            if ((userAttributes[attribute] ?: 0) < value) return false
        }
        
        requiredItems.forEach { item ->
            if (!userItems.contains(item)) return false
        }
        
        requiredAchievements.forEach { achievement ->
            if (!completedAchievements.contains(achievement)) return false
        }
        
        requiredQuests.forEach { quest ->
            if (!completedQuests.contains(quest)) return false
        }
        
        requiredBuffs.forEach { buff ->
            if (!activeBuffs.contains(buff)) return false
        }
        
        forbiddenBuffs.forEach { buff ->
            if (activeBuffs.contains(buff)) return false
        }
        
        return true
    }
}

@Serializable
data class BuffConfig(
    val version: String,
    val buffs: Map<String, BuffDefinition>,
    val defaultBuffId: String,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    fun getBuff(buffId: String): BuffDefinition? {
        return buffs[buffId]
    }
    
    fun getAllBuffs(): Map<String, BuffDefinition> {
        return buffs
    }
    
    fun getBuffsByType(buffType: BuffType): List<BuffDefinition> {
        return buffs.values.filter { it.buffType == buffType }
    }
    
    fun getBuffsByCategory(buffCategory: BuffCategory): List<BuffDefinition> {
        return buffs.values.filter { it.buffCategory == buffCategory }
    }
    
    fun getPermanentBuffs(): List<BuffDefinition> {
        return buffs.values.filter { it.isPermanent() }
    }
    
    fun getTemporaryBuffs(): List<BuffDefinition> {
        return buffs.values.filter { !it.isPermanent() }
    }
    
    fun getStackableBuffs(): List<BuffDefinition> {
        return buffs.values.filter { it.canStack() }
    }
    
    fun getDispellableBuffs(): List<BuffDefinition> {
        return buffs.values.filter { it.canBeDispelled() }
    }
    
    fun getVisibleBuffs(): List<BuffDefinition> {
        return buffs.values.filter { it.isVisible() }
    }
    
    fun getBuffsWithConflicts(): List<BuffDefinition> {
        return buffs.values.filter { it.hasConflicts() }
    }
    
    fun getDefaultBuff(): BuffDefinition? {
        return buffs[defaultBuffId]
    }
    
    fun getBuffCount(): Int {
        return buffs.size
    }
    
    fun getBuffCountByType(): Map<BuffType, Int> {
        return BuffType.values().associateWith { type ->
            getBuffsByType(type).size
        }
    }
    
    fun getBuffCountByCategory(): Map<BuffCategory, Int> {
        return BuffCategory.values().associateWith { category ->
            getBuffsByCategory(category).size
        }
    }
    
    fun getConfigVersionValue(): String {
        return version
    }
    
    fun getLastUpdatedAt(): Long {
        return lastUpdateTime
    }
    
    fun getFormattedLastUpdateTime(): String {
        val date = java.util.Date(lastUpdateTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceLastUpdate(): Long {
        return System.currentTimeMillis() - lastUpdateTime
    }
    
    fun getTimeSinceLastUpdateMinutes(): Float {
        return getTimeSinceLastUpdate() / 60000f
    }
}

@Serializable
data class BuffHistoryEntry(
    val id: String,
    val userId: String,
    val buffId: String,
    val action: BuffAction,
    val oldState: BuffState?,
    val newState: BuffState?,
    val source: BuffSource,
    val sourceId: String?,
    val timestamp: Long
) {
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceAction(): Long {
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceActionMinutes(): Float {
        return getTimeSinceAction() / 60000f
    }
    
    fun getActionDescription(): String {
        return when (action) {
            BuffAction.APPLY -> "Applied"
            BuffAction.REFRESH -> "Refreshed"
            BuffAction.STACK -> "Stacked"
            BuffAction.EXPIRE -> "Expired"
            BuffAction.DISPEL -> "Dispelled"
            BuffAction.PAUSE -> "Paused"
            BuffAction.RESUME -> "Resumed"
            BuffAction.REMOVE -> "Removed"
        }
    }
}

enum class BuffType {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

enum class BuffCategory {
    ATTRIBUTE,
    CURRENCY,
    EXPERIENCE,
    SKILL,
    MOVEMENT,
    COMBAT,
    RESOURCE,
    CUSTOM
}

enum class BuffStackStrategy {
    NO_STACK,
    REFRESH_DURATION,
    EXTEND_DURATION,
    STACK_INTENSITY,
    STACK_INTENSITY_MAX_3
}

enum class BuffRefreshStrategy {
    REFRESH_ON_APPLY,
    REFRESH_ON_EXPIRE,
    NO_REFRESH
}

enum class ModifierType {
    ATTRIBUTE_MULTIPLIER,
    ATTRIBUTE_BONUS,
    CURRENCY_MULTIPLIER,
    CURRENCY_BONUS,
    SKILL_COOLDOWN,
    SKILL_DAMAGE,
    SKILL_DURATION,
    MOVEMENT_SPEED,
    MOVEMENT_RANGE,
    COMBAT_DAMAGE,
    COMBAT_DEFENSE,
    COMBAT_SPEED,
    CUSTOM
}

enum class ModifierTarget {
    SELF,
    TARGET,
    ALL,
    PARTY,
    ENEMIES
}

enum class BuffAction {
    APPLY,
    REFRESH,
    STACK,
    EXPIRE,
    DISPEL,
    PAUSE,
    RESUME,
    REMOVE
}

enum class BuffSource {
    ITEM_USE,
    SKILL_USE,
    EVENT,
    QUEST,
    ACHIEVEMENT,
    GACHA,
    SHOP,
    ADMIN,
    SYSTEM,
    PASSIVE
}
