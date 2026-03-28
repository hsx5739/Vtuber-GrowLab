package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class SkinDefinition(
    val skinId: String,
    val skinName: String,
    val skinDescription: String,
    val rarity: SkinRarity,
    val skinType: SkinType,
    val baseSkinId: String?,
    val iconPath: String,
    val previewImagePath: String,
    val fullImagePath: String,
    val unlockSources: List<SkinUnlockSource>,
    val gachaPoolIds: List<String>,
    val shardCost: Int,
    val bondBonus: BondBonus,
    val skinAttributes: SkinAttributes,
    val availability: SkinAvailability,
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
    
    fun canBeObtainedFromGacha(): Boolean {
        return gachaPoolIds.isNotEmpty() && isAvailable()
    }
    
    fun canBeSynthesizedFromShards(): Boolean {
        return shardCost > 0 && isAvailable()
    }
    
    fun getUnlockSourceNames(): List<String> {
        return unlockSources.map { it.name }
    }
    
    fun getRarityDisplayName(): String {
        return when (rarity) {
            SkinRarity.N -> "Normal"
            SkinRarity.R -> "Rare"
            SkinRarity.SR -> "Super Rare"
            SkinRarity.SSR -> "Super Super Rare"
        }
    }
    
    fun getSkinTypeDisplayName(): String {
        return when (skinType) {
            SkinType.DEFAULT -> "Default"
            SkinType.GACHA -> "Gacha"
            SkinType.EVENT -> "Event"
            SkinType.ACHIEVEMENT -> "Achievement"
            SkinType.SHOP -> "Shop"
            SkinType.SPECIAL -> "Special"
            SkinType.LIMITED -> "Limited"
        }
    }
    
    fun getTotalBondBonusAtLevel(level: Int): Float {
        return bondBonus.baseBonus + (bondBonus.bonusPerLevel * level)
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
    
    fun isLimitedTime(): Boolean {
        return availability.startTime != null || availability.endTime != null
    }
    
    fun isExclusive(): Boolean {
        return availability.isExclusive
    }
}

@Serializable
data class BondBonus(
    val baseBonus: Float,
    val bonusPerLevel: Float,
    val maxLevel: Int,
    val affectedAttributes: List<AttributeType>
) {
    fun getBonusAtLevel(level: Int): Float {
        val effectiveLevel = level.coerceIn(0, maxLevel)
        return baseBonus + (bonusPerLevel * effectiveLevel)
    }
    
    fun getMaxBonus(): Float {
        return getBonusAtLevel(maxLevel)
    }
    
    fun getBonusPercentageAtLevel(level: Int): Float {
        return getBonusAtLevel(level) * 100f
    }
    
    fun getAffectedAttributeNames(): List<String> {
        return affectedAttributes.map { it.name }
    }
}

@Serializable
data class SkinAttributes(
    val moodBonus: Int = 0,
    val vitalityBonus: Int = 0,
    val fortuneBonus: Int = 0,
    val focusBonus: Int = 0,
    val bondBonus: Int = 0,
    val customAttributes: Map<String, Int> = emptyMap()
) {
    fun getAttributeBonus(attribute: AttributeType): Int {
        return when (attribute) {
            AttributeType.MOOD -> moodBonus
            AttributeType.VITALITY -> vitalityBonus
            AttributeType.FORTUNE -> fortuneBonus
            AttributeType.ENERGY -> 0
            AttributeType.FOCUS -> focusBonus
            AttributeType.BOND -> bondBonus
            AttributeType.EXPERIENCE -> 0
        }
    }
    
    fun getTotalBonus(): Int {
        return moodBonus + vitalityBonus + fortuneBonus + focusBonus + bondBonus +
               customAttributes.values.sum()
    }
    
    fun getHighestBonus(): Pair<AttributeType, Int> {
        val bonuses = mapOf(
            AttributeType.MOOD to moodBonus,
            AttributeType.VITALITY to vitalityBonus,
            AttributeType.FORTUNE to fortuneBonus,
            AttributeType.FOCUS to focusBonus,
            AttributeType.BOND to bondBonus
        )
        
        val best = bonuses.maxByOrNull { it.value }
        return best?.toPair() ?: (AttributeType.MOOD to 0)
    }
    
    fun getLowestBonus(): Pair<AttributeType, Int> {
        val bonuses = mapOf(
            AttributeType.MOOD to moodBonus,
            AttributeType.VITALITY to vitalityBonus,
            AttributeType.FORTUNE to fortuneBonus,
            AttributeType.FOCUS to focusBonus,
            AttributeType.BOND to bondBonus
        )
        
        val lowest = bonuses.minByOrNull { it.value }
        return lowest?.toPair() ?: (AttributeType.MOOD to 0)
    }
}

@Serializable
data class SkinAvailability(
    val isAvailable: Boolean = true,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val isExclusive: Boolean = false,
    val requiredLevel: Int? = null,
    val requiredAchievements: List<String> = emptyList(),
    val requiredQuests: List<String> = emptyList(),
    val requiredEvents: List<String> = emptyList()
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
        
        return true
    }
    
    fun hasRequirements(): Boolean {
        return requiredLevel != null ||
               requiredAchievements.isNotEmpty() ||
               requiredQuests.isNotEmpty() ||
               requiredEvents.isNotEmpty()
    }
    
    fun meetsRequirements(userLevel: Int, completedAchievements: List<String>, completedQuests: List<String>, completedEvents: List<String>): Boolean {
        requiredLevel?.let { level ->
            if (userLevel < level) return false
        }
        
        requiredAchievements.forEach { achievement ->
            if (!completedAchievements.contains(achievement)) return false
        }
        
        requiredQuests.forEach { quest ->
            if (!completedQuests.contains(quest)) return false
        }
        
        requiredEvents.forEach { event ->
            if (!completedEvents.contains(event)) return false
        }
        
        return true
    }
}

@Serializable
data class SkinConfig(
    val version: String,
    val skins: Map<String, SkinDefinition>,
    val defaultSkinId: String,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    fun getSkin(skinId: String): SkinDefinition? {
        return skins[skinId]
    }
    
    fun getAllSkins(): Map<String, SkinDefinition> {
        return skins
    }
    
    fun getSkinsByRarity(rarity: SkinRarity): List<SkinDefinition> {
        return skins.values.filter { it.rarity == rarity }
    }
    
    fun getSkinsByType(type: SkinType): List<SkinDefinition> {
        return skins.values.filter { it.skinType == type }
    }
    
    fun getAvailableSkins(): List<SkinDefinition> {
        return skins.values.filter { it.isAvailable() }
    }
    
    fun getLimitedTimeSkins(): List<SkinDefinition> {
        return skins.values.filter { it.isLimitedTime() }
    }
    
    fun getExclusiveSkins(): List<SkinDefinition> {
        return skins.values.filter { it.isExclusive() }
    }
    
    fun getDefaultSkin(): SkinDefinition? {
        return skins[defaultSkinId]
    }
    
    fun getSkinCount(): Int {
        return skins.size
    }
    
    fun getAvailableSkinCount(): Int {
        return getAvailableSkins().size
    }
    
    fun getSkinCountByRarity(): Map<SkinRarity, Int> {
        return SkinRarity.values().associateWith { rarity ->
            getSkinsByRarity(rarity).size
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
data class SkinUnlockPath(
    val skinId: String,
    val source: SkinUnlockSource,
    val sourceId: String?,
    val cost: Int,
    val costType: CostType,
    val requirements: List<String>,
    val isAvailable: Boolean,
    val availabilityStartTime: Long?,
    val availabilityEndTime: Long?
) {
    fun isCurrentlyAvailable(): Boolean {
        if (!isAvailable) return false
        
        val currentTime = System.currentTimeMillis()
        
        if (availabilityStartTime != null && currentTime < availabilityStartTime) {
            return false
        }
        
        if (availabilityEndTime != null && currentTime > availabilityEndTime) {
            return false
        }
        
        return true
    }
    
    fun getCostDescription(): String {
        return "$costType: $cost"
    }
    
    fun getSourceDescription(): String {
        return when (source) {
            SkinUnlockSource.GACHA -> "Gacha Pool"
            SkinUnlockSource.SHARD_SYNTHESIS -> "Shard Synthesis"
            SkinUnlockSource.SHOP -> "Shop"
            SkinUnlockSource.EVENT -> "Event Reward"
            SkinUnlockSource.ACHIEVEMENT -> "Achievement Reward"
            SkinUnlockSource.GIFT -> "Gift"
            SkinUnlockSource.DEFAULT -> "Default"
        }
    }
    
    fun getTimeUntilAvailable(): Long? {
        val startTime = availabilityStartTime ?: return 0L
        val currentTime = System.currentTimeMillis()
        return (startTime - currentTime).coerceAtLeast(0L)
    }
    
    fun getTimeUntilExpired(): Long? {
        val endTime = availabilityEndTime ?: return null
        val currentTime = System.currentTimeMillis()
        return (endTime - currentTime).coerceAtLeast(0L)
    }
}

enum class SkinType {
    DEFAULT,
    GACHA,
    EVENT,
    ACHIEVEMENT,
    SHOP,
    SPECIAL,
    LIMITED
}

enum class CostType {
    STARDUST,
    MOONGLOW,
    GACHA_TICKETS,
    SHARDS,
    CURRENCY,
    REAL_MONEY,
    ACHIEVEMENT_POINTS,
    EVENT_POINTS
}

@Serializable
data class SkinPreview(
    val skinId: String,
    val skinName: String,
    val rarity: SkinRarity,
    val iconPath: String,
    val previewImagePath: String,
    val isUnlocked: Boolean,
    val isEquipped: Boolean,
    val bondLevel: Int,
    val shardCount: Int,
    val unlockProgress: Float,
    val canUnlock: Boolean,
    val unlockCost: Int?,
    val unlockSource: SkinUnlockSource?
) {
    fun getUnlockProgressPercentage(): Int {
        return (unlockProgress * 100).toInt()
    }
    
    fun getRarityColor(): String {
        return when (rarity) {
            SkinRarity.N -> "#FFFFFF"
            SkinRarity.R -> "#00BFFF"
            SkinRarity.SR -> "#FFD700"
            SkinRarity.SSR -> "#FF69B4"
        }
    }
    
    fun getRarityStarCount(): Int {
        return when (rarity) {
            SkinRarity.N -> 1
            SkinRarity.R -> 2
            SkinRarity.SR -> 3
            SkinRarity.SSR -> 4
        }
    }
}

@Serializable
data class SkinDetail(
    val skinId: String,
    val skinName: String,
    val skinDescription: String,
    val rarity: SkinRarity,
    val skinType: SkinType,
    val iconPath: String,
    val previewImagePath: String,
    val fullImagePath: String,
    val isUnlocked: Boolean,
    val isEquipped: Boolean,
    val bondLevel: Int,
    val bondExperience: Int,
    val shardCount: Int,
    val usageCount: Int,
    val unlockSources: List<SkinUnlockSource>,
    val bondBonus: BondBonus,
    val skinAttributes: SkinAttributes,
    val availability: SkinAvailability,
    val unlockPaths: List<SkinUnlockPath>,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getBondProgress(): Float {
        val maxExperience = bondBonus.maxLevel * 100
        return bondExperience.toFloat() / maxExperience.toFloat()
    }
    
    fun getBondProgressPercentage(): Int {
        return (getBondProgress() * 100).toInt()
    }
    
    fun getShardProgress(): Float {
        return shardCount.toFloat() / 100f
    }
    
    fun getShardProgressPercentage(): Int {
        return (getShardProgress() * 100).toInt()
    }
    
    fun getShardsNeeded(): Int {
        return (100 - shardCount).coerceAtLeast(0)
    }
    
    fun canSynthesize(): Boolean {
        return shardCount >= 100 && !isUnlocked
    }
    
    fun getCurrentBondBonus(): Float {
        return bondBonus.getBonusAtLevel(bondLevel)
    }
    
    fun getMaxBondBonus(): Float {
        return bondBonus.getMaxBonus()
    }
    
    fun getTotalAttributeBonus(): Int {
        return skinAttributes.getTotalBonus()
    }
    
    fun getRarityDisplayName(): String {
        return when (rarity) {
            SkinRarity.N -> "Normal"
            SkinRarity.R -> "Rare"
            SkinRarity.SR -> "Super Rare"
            SkinRarity.SSR -> "Super Super Rare"
        }
    }
    
    fun getRarityColor(): String {
        return when (rarity) {
            SkinRarity.N -> "#FFFFFF"
            SkinRarity.R -> "#00BFFF"
            SkinRarity.SR -> "#FFD700"
            SkinRarity.SSR -> "#FF69B4"
        }
    }
    
    fun getRarityStarCount(): Int {
        return when (rarity) {
            SkinRarity.N -> 1
            SkinRarity.R -> 2
            SkinRarity.SR -> 3
            SkinRarity.SSR -> 4
        }
    }
}
