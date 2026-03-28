package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemDefinition(
    val itemId: String,
    val itemName: String,
    val itemDescription: String,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val iconPath: String,
    val isConsumable: Boolean,
    val isEquippable: Boolean,
    val isStackable: Boolean,
    val maxStack: Int,
    val useRewardBundleId: String?,
    val buffId: String?,
    val currencyRewards: Map<CurrencyType, Int>,
    val attributeChanges: Map<AttributeType, Int>,
    val itemEffects: List<ItemEffect>,
    val usageLimit: UsageLimit,
    val cooldown: CooldownInfo,
    val requirements: ItemRequirements,
    val availability: ItemAvailability,
    val metadata: Map<String, String> = emptyMap()
) {
    fun isAvailable(): Boolean {
        val currentTime = System.currentTimeMillis()
        
        if (availability.startTime != null && currentTime < availability.startTime) {
            return false
        }
        
        if (availability.endTime != null && currentTime > availability.endTime) {
            return false
        }
        
        if (!availability.isAvailable) {
            return false
        }
        
        return true
    }
    
    fun canBeUsed(): Boolean {
        return isConsumable && useRewardBundleId != null
    }
    
    fun canBeEquipped(): Boolean {
        return isEquippable
    }
    
    fun canBeStacked(): Boolean {
        return isStackable
    }
    
    fun hasBuff(): Boolean {
        return buffId != null
    }
    
    fun hasRewards(): Boolean {
        return currencyRewards.isNotEmpty() || attributeChanges.isNotEmpty()
    }
    
    fun hasEffects(): Boolean {
        return itemEffects.isNotEmpty()
    }
    
    fun hasUsageLimit(): Boolean {
        return usageLimit.maxUsageCount != null || usageLimit.maxUsagePerDay != null
    }
    
    fun hasCooldown(): Boolean {
        return cooldown.cooldownTime != null
    }
    
    fun hasRequirements(): Boolean {
        return requirements.minLevel != null ||
               requirements.requiredItems.isNotEmpty() ||
               requirements.requiredAchievements.isNotEmpty() ||
               requirements.requiredCurrencies.isNotEmpty()
    }
    
    fun isLimitedTime(): Boolean {
        return availability.startTime != null || availability.endTime != null
    }
    
    fun isExclusive(): Boolean {
        return availability.isExclusive
    }
    
    fun getItemTypeDisplayName(): String {
        return when (itemType) {
            ItemType.ITEM -> "Item"
            ItemType.SKILL_CARD -> "Skill Card"
            ItemType.SKIN -> "Skin"
            ItemType.SHARD -> "Shard"
            ItemType.CURRENCY -> "Currency"
            ItemType.BUFF -> "Buff"
            ItemType.CHAT_BUBBLE -> "Chat Bubble"
            ItemType.EMOJI_PACK -> "Emoji Pack"
            ItemType.AVATAR_FRAME -> "Avatar Frame"
            ItemType.THEME -> "Theme"
        }
    }
    
    fun getRarityDisplayName(): String {
        return when (rarity) {
            ItemRarity.COMMON -> "Common"
            ItemRarity.UNCOMMON -> "Uncommon"
            ItemRarity.RARE -> "Rare"
            ItemRarity.EPIC -> "Epic"
            ItemRarity.LEGENDARY -> "Legendary"
        }
    }
    
    fun getRarityColor(): String {
        return when (rarity) {
            ItemRarity.COMMON -> "#FFFFFF"
            ItemRarity.UNCOMMON -> "#00FF00"
            ItemRarity.RARE -> "#00BFFF"
            ItemRarity.EPIC -> "#FFD700"
            ItemRarity.LEGENDARY -> "#FF69B4"
        }
    }
    
    fun getFormattedStartTime(): String? {
        val timestamp = availability.startTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getFormattedEndTime(): String? {
        val timestamp = availability.endTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeUntilAvailable(): Long? {
        val startTime = availability.startTime ?: return 0L
        val currentTime = System.currentTimeMillis()
        return (startTime - currentTime).coerceAtLeast(0L)
    }
    
    fun getTimeUntilExpired(): Long? {
        val endTime = availability.endTime ?: return null
        val currentTime = System.currentTimeMillis()
        return (endTime - currentTime).coerceAtLeast(0L)
    }
    
    fun getTotalCurrencyReward(): Int {
        return currencyRewards.values.sum()
    }
    
    fun getTotalAttributeBonus(): Int {
        return attributeChanges.values.sum()
    }
}

@Serializable
data class UsageLimit(
    val maxUsageCount: Int?,
    val maxUsagePerDay: Int?,
    val maxUsagePerWeek: Int?,
    val maxUsagePerMonth: Int?,
    val resetTime: Long?
) {
    fun hasGlobalLimit(): Boolean {
        return maxUsageCount != null
    }
    
    fun hasDailyLimit(): Boolean {
        return maxUsagePerDay != null
    }
    
    fun hasWeeklyLimit(): Boolean {
        return maxUsagePerWeek != null
    }
    
    fun hasMonthlyLimit(): Boolean {
        return maxUsagePerMonth != null
    }
    
    fun hasResetTime(): Boolean {
        return resetTime != null
    }
    
    fun getFormattedResetTime(): String? {
        val timestamp = resetTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
data class CooldownInfo(
    val cooldownTime: Long?,
    val cooldownType: CooldownType?
) {
    fun hasCooldown(): Boolean {
        return cooldownTime != null
    }
    
    fun getCooldownDuration(): Long? {
        return cooldownTime
    }
    
    fun getCooldownDurationMinutes(): Float? {
        return cooldownTime?.let { it / 60000f }
    }
    
    fun getCooldownTypeDisplayName(): String? {
        return cooldownType?.name
    }
}

@Serializable
data class ItemRequirements(
    val minLevel: Int?,
    val maxLevel: Int?,
    val requiredItems: Map<String, Int>,
    val requiredAchievements: List<String>,
    val requiredCurrencies: Map<CurrencyType, Int>,
    val requiredAttributes: Map<AttributeType, Int>
) {
    fun hasLevelRequirement(): Boolean {
        return minLevel != null || maxLevel != null
    }
    
    fun hasItemRequirements(): Boolean {
        return requiredItems.isNotEmpty()
    }
    
    fun hasAchievementRequirements(): Boolean {
        return requiredAchievements.isNotEmpty()
    }
    
    fun hasCurrencyRequirements(): Boolean {
        return requiredCurrencies.isNotEmpty()
    }
    
    fun hasAttributeRequirements(): Boolean {
        return requiredAttributes.isNotEmpty()
    }
    
    fun hasRequirements(): Boolean {
        return hasLevelRequirement() ||
               hasItemRequirements() ||
               hasAchievementRequirements() ||
               hasCurrencyRequirements() ||
               hasAttributeRequirements()
    }
    
    fun meetsRequirements(
        userLevel: Int,
        userItems: Map<String, Int>,
        completedAchievements: List<String>,
        userCurrencies: Map<CurrencyType, Int>,
        userAttributes: Map<AttributeType, Int>
    ): Boolean {
        minLevel?.let { level ->
            if (userLevel < level) return false
        }
        
        maxLevel?.let { level ->
            if (userLevel > level) return false
        }
        
        requiredItems.forEach { (itemId, count) ->
            if ((userItems[itemId] ?: 0) < count) return false
        }
        
        requiredAchievements.forEach { achievement ->
            if (!completedAchievements.contains(achievement)) return false
        }
        
        requiredCurrencies.forEach { (currency, amount) ->
            if ((userCurrencies[currency] ?: 0) < amount) return false
        }
        
        requiredAttributes.forEach { (attribute, value) ->
            if ((userAttributes[attribute] ?: 0) < value) return false
        }
        
        return true
    }
}

@Serializable
data class ItemAvailability(
    val isAvailable: Boolean = true,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val isExclusive: Boolean = false,
    val stock: Int? = null,
    val maxStock: Int? = null
) {
    fun isTimeLimited(): Boolean {
        return startTime != null || endTime != null
    }
    
    fun isCurrentlyAvailable(): Boolean {
        if (!isAvailable) return false
        
        val currentTime = System.currentTimeMillis()
        
        if (startTime != null && currentTime < startTime) {
            return false
        }
        
        if (endTime != null && currentTime > endTime) {
            return false
        }
        
        if (stock != null && stock <= 0) {
            return false
        }
        
        return true
    }
    
    fun isOutOfStock(): Boolean {
        return stock != null && stock <= 0
    }
    
    fun isLimitedStock(): Boolean {
        return maxStock != null
    }
    
    fun getStockPercentage(): Float? {
        val currentStock = stock ?: return null
        val maxStockValue = maxStock ?: return null
        return if (maxStockValue > 0) {
            (currentStock.toFloat() / maxStockValue.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getRemainingStock(): Int? {
        val currentStock = stock ?: return null
        val maxStockValue = maxStock ?: return currentStock
        return (maxStockValue - currentStock).coerceAtLeast(0)
    }
}

@Serializable
data class ItemConfig(
    val version: String,
    val items: Map<String, ItemDefinition>,
    val defaultItemId: String,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    fun getItem(itemId: String): ItemDefinition? {
        return items[itemId]
    }
    
    fun getAllItems(): Map<String, ItemDefinition> {
        return items
    }
    
    fun getItemsByType(itemType: ItemType): List<ItemDefinition> {
        return items.values.filter { it.itemType == itemType }
    }
    
    fun getItemsByRarity(rarity: ItemRarity): List<ItemDefinition> {
        return items.values.filter { it.rarity == rarity }
    }
    
    fun getAvailableItems(): List<ItemDefinition> {
        return items.values.filter { it.isAvailable() }
    }
    
    fun getConsumableItems(): List<ItemDefinition> {
        return items.values.filter { it.isConsumable }
    }
    
    fun getEquippableItems(): List<ItemDefinition> {
        return items.values.filter { it.isEquippable }
    }
    
    fun getStackableItems(): List<ItemDefinition> {
        return items.values.filter { it.isStackable }
    }
    
    fun getItemsWithBuffs(): List<ItemDefinition> {
        return items.values.filter { it.hasBuff() }
    }
    
    fun getItemsWithRewards(): List<ItemDefinition> {
        return items.values.filter { it.hasRewards() }
    }
    
    fun getDefaultItem(): ItemDefinition? {
        return items[defaultItemId]
    }
    
    fun getItemCount(): Int {
        return items.size
    }
    
    fun getAvailableItemCount(): Int {
        return getAvailableItems().size
    }
    
    fun getItemCountByType(): Map<ItemType, Int> {
        return ItemType.values().associateWith { type ->
            getItemsByType(type).size
        }
    }
    
    fun getItemCountByRarity(): Map<ItemRarity, Int> {
        return ItemRarity.values().associateWith { rarity ->
            getItemsByRarity(rarity).size
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
data class ItemEffect(
    val effectId: String,
    val effectType: EffectType,
    val effectValue: Int,
    val effectDescription: String,
    val duration: Long?,
    val target: ItemEffectTarget,
    val isImmediate: Boolean
) {
    fun hasDuration(): Boolean {
        return duration != null
    }
    
    fun getDurationMinutes(): Float? {
        return duration?.let { it / 60000f }
    }
    
    fun formatEffectDescription(): String {
        return if (isImmediate) {
            "Immediate: $effectDescription"
        } else {
            val durationText = getDurationMinutes()?.let { "${String.format("%.1f", it)} min" } ?: "Permanent"
            "Duration: $durationText, $effectDescription"
        }
    }
}

@Serializable
data class ItemPreview(
    val itemId: String,
    val itemName: String,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val iconPath: String,
    val isAvailable: Boolean,
    val isUnlocked: Boolean,
    val count: Int,
    val canUse: Boolean,
    val canEquip: Boolean,
    val cooldownRemaining: Long?,
    val usageLimitRemaining: Int?
) {
    fun getRarityColor(): String {
        return when (rarity) {
            ItemRarity.COMMON -> "#FFFFFF"
            ItemRarity.UNCOMMON -> "#00FF00"
            ItemRarity.RARE -> "#00BFFF"
            ItemRarity.EPIC -> "#FFD700"
            ItemRarity.LEGENDARY -> "#FF69B4"
        }
    }
    
    fun getCooldownRemainingMinutes(): Float? {
        return cooldownRemaining?.let { it / 60000f }
    }
    
    fun getFormattedCooldownRemaining(): String? {
        val minutes = getCooldownRemainingMinutes() ?: return null
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
}

@Serializable
data class ItemDetail(
    val itemId: String,
    val itemName: String,
    val itemDescription: String,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val iconPath: String,
    val isAvailable: Boolean,
    val isUnlocked: Boolean,
    val count: Int,
    val isConsumable: Boolean,
    val isEquippable: Boolean,
    val isStackable: Boolean,
    val maxStack: Int,
    val useRewardBundleId: String?,
    val buffId: String?,
    val currencyRewards: Map<CurrencyType, Int>,
    val attributeChanges: Map<AttributeType, Int>,
    val itemEffects: List<ItemEffect>,
    val usageLimit: UsageLimit,
    val cooldown: CooldownInfo,
    val requirements: ItemRequirements,
    val availability: ItemAvailability,
    val metadata: Map<String, String> = emptyMap()
) {
    fun canBeUsed(): Boolean {
        return isConsumable && useRewardBundleId != null && isAvailable
    }
    
    fun canBeEquipped(): Boolean {
        return isEquippable && isAvailable
    }
    
    fun hasBuff(): Boolean {
        return buffId != null
    }
    
    fun hasRewards(): Boolean {
        return currencyRewards.isNotEmpty() || attributeChanges.isNotEmpty()
    }
    
    fun hasEffects(): Boolean {
        return itemEffects.isNotEmpty()
    }
    
    fun hasUsageLimit(): Boolean {
        return usageLimit.hasGlobalLimit() || usageLimit.hasDailyLimit()
    }
    
    fun hasCooldown(): Boolean {
        return cooldown.hasCooldown()
    }
    
    fun hasRequirements(): Boolean {
        return requirements.hasRequirements()
    }
    
    fun getRarityDisplayName(): String {
        return when (rarity) {
            ItemRarity.COMMON -> "Common"
            ItemRarity.UNCOMMON -> "Uncommon"
            ItemRarity.RARE -> "Rare"
            ItemRarity.EPIC -> "Epic"
            ItemRarity.LEGENDARY -> "Legendary"
        }
    }
    
    fun getRarityColor(): String {
        return when (rarity) {
            ItemRarity.COMMON -> "#FFFFFF"
            ItemRarity.UNCOMMON -> "#00FF00"
            ItemRarity.RARE -> "#00BFFF"
            ItemRarity.EPIC -> "#FFD700"
            ItemRarity.LEGENDARY -> "#FF69B4"
        }
    }
    
    fun getTotalCurrencyReward(): Int {
        return currencyRewards.values.sum()
    }
    
    fun getTotalAttributeBonus(): Int {
        return attributeChanges.values.sum()
    }
}

enum class ItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

enum class CooldownType {
    GLOBAL,
    PER_ITEM,
    PER_USER,
    PER_SESSION
}

enum class ItemEffectTarget {
    SELF,
    TARGET,
    ALL,
    RANDOM,
    PARTY
}
