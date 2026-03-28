package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Inventory(
    val userId: String,
    val skillCards: Map<String, SkillCardState>,
    val items: Map<String, ItemState>,
    val skins: Map<String, SkinState>,
    val shards: Map<String, ShardState>,
    val currencies: Map<CurrencyType, Int>,
    val equippedSkinId: String? = null,
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) {
    fun hasSkillCard(skillCardId: String): Boolean {
        return skillCards.containsKey(skillCardId)
    }
    
    fun getSkillCardState(skillCardId: String): SkillCardState? {
        return skillCards[skillCardId]
    }
    
    fun hasItem(itemId: String): Boolean {
        return items.containsKey(itemId)
    }
    
    fun getItemState(itemId: String): ItemState? {
        return items[itemId]
    }
    
    fun hasSkin(skinId: String): Boolean {
        return skins.containsKey(skinId)
    }
    
    fun getSkinState(skinId: String): SkinState? {
        return skins[skinId]
    }
    
    fun hasShard(skinId: String): Boolean {
        return shards.containsKey(skinId)
    }
    
    fun getShardState(skinId: String): ShardState? {
        return shards[skinId]
    }
    
    fun getCurrency(currencyType: CurrencyType): Int {
        return currencies[currencyType] ?: 0
    }
    
    fun isSkinEquipped(skinId: String): Boolean {
        return equippedSkinId == skinId
    }
    
    fun getEquippedSkinIdOrNull(): String? {
        return equippedSkinId
    }
    
    fun getSkillCardCount(): Int {
        return skillCards.size
    }
    
    fun getItemCount(): Int {
        return items.size
    }
    
    fun getSkinCount(): Int {
        return skins.size
    }
    
    fun getShardCount(): Int {
        return shards.size
    }
    
    fun getTotalItemCount(): Int {
        return items.values.sumOf { it.count }
    }
    
    fun getTotalShardCount(): Int {
        return shards.values.sumOf { it.count }
    }
    
    fun getUnlockedSkillCards(): List<String> {
        return skillCards.filter { it.value.isUnlocked }.keys.toList()
    }
    
    fun getLockedSkillCards(): List<String> {
        return skillCards.filter { !it.value.isUnlocked }.keys.toList()
    }
    
    fun getEquippedItems(): List<String> {
        return items.filter { it.value.isEquipped }.keys.toList()
    }
    
    fun getUnlockedSkins(): List<String> {
        return skins.filter { it.value.isUnlocked }.keys.toList()
    }
    
    fun getLockedSkins(): List<String> {
        return skins.filter { !it.value.isUnlocked }.keys.toList()
    }
    
    fun getTimeSinceLastUpdate(): Long {
        return System.currentTimeMillis() - lastUpdateTime
    }
    
    fun getTimeSinceLastUpdateMinutes(): Float {
        return getTimeSinceLastUpdate() / 60000f
    }
}

@Serializable
data class SkillCardState(
    val skillCardId: String,
    val isUnlocked: Boolean,
    val unlockTime: Long? = null,
    val level: Int = 1,
    val experience: Int = 0,
    val usageCount: Int = 0,
    val lastUsedTime: Long? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getUnlockStatus(): String {
        return if (isUnlocked) "Unlocked" else "Locked"
    }
    
    fun getFormattedUnlockTime(): String? {
        val timestamp = unlockTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceUnlock(): Long? {
        val timestamp = unlockTime ?: return null
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceUnlockMinutes(): Float? {
        return getTimeSinceUnlock()?.let { it / 60000f }
    }
    
    fun getFormattedLastUsedTime(): String? {
        val timestamp = lastUsedTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceLastUse(): Long? {
        val timestamp = lastUsedTime ?: return null
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceLastUseMinutes(): Float? {
        return getTimeSinceLastUse()?.let { it / 60000f }
    }
    
    fun getUsageFrequency(): Float {
        val timeSinceUnlock = getTimeSinceUnlock() ?: return 0f
        val timeSinceUnlockMinutes = timeSinceUnlock / 60000f
        return if (timeSinceUnlockMinutes > 0) {
            usageCount.toFloat() / timeSinceUnlockMinutes
        } else {
            0f
        }
    }
    
    fun getLevelProgress(): Float {
        return experience.toFloat() / getExperienceForNextLevel()
    }
    
    fun getLevelProgressPercentage(): Int {
        return (getLevelProgress() * 100).toInt()
    }
    
    fun getExperienceForNextLevel(): Int {
        return level * 100
    }
    
    fun canLevelUp(): Boolean {
        return experience >= getExperienceForNextLevel()
    }
}

@Serializable
data class ItemState(
    val itemId: String,
    val count: Int,
    val isEquipped: Boolean = false,
    val isConsumable: Boolean = false,
    val maxStack: Int = 999,
    val acquireTime: Long = System.currentTimeMillis(),
    val lastUsedTime: Long? = null,
    val expireTime: Long? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getFormattedAcquireTime(): String {
        val date = java.util.Date(acquireTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceAcquire(): Long {
        return System.currentTimeMillis() - acquireTime
    }
    
    fun getTimeSinceAcquireMinutes(): Float {
        return getTimeSinceAcquire() / 60000f
    }
    
    fun getFormattedLastUsedTime(): String? {
        val timestamp = lastUsedTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceLastUse(): Long? {
        val timestamp = lastUsedTime ?: return null
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceLastUseMinutes(): Float? {
        return getTimeSinceLastUse()?.let { it / 60000f }
    }
    
    fun getFormattedExpireTime(): String? {
        val timestamp = expireTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeUntilExpire(): Long? {
        val timestamp = expireTime ?: return null
        return timestamp - System.currentTimeMillis()
    }
    
    fun getTimeUntilExpireMinutes(): Float? {
        return getTimeUntilExpire()?.let { it / 60000f }
    }
    
    fun isExpired(): Boolean {
        val timestamp = expireTime ?: return false
        return System.currentTimeMillis() > timestamp
    }
    
    fun isExpiringSoon(thresholdMinutes: Int = 60): Boolean {
        val timeUntilExpire = getTimeUntilExpire() ?: return false
        return timeUntilExpire > 0 && timeUntilExpire < thresholdMinutes * 60000L
    }
    
    fun getRemainingStack(): Int {
        return (maxStack - count).coerceAtLeast(0)
    }
    
    fun isFullStack(): Boolean {
        return count >= maxStack
    }
    
    fun getStackPercentage(): Float {
        return count.toFloat() / maxStack.toFloat()
    }
    
    fun getStackPercentageInt(): Int {
        return (getStackPercentage() * 100).toInt()
    }
}

@Serializable
data class SkinState(
    val skinId: String,
    val isUnlocked: Boolean,
    val unlockTime: Long? = null,
    val unlockSource: SkinUnlockSource? = null,
    val shardCount: Int = 0,
    val isEquipped: Boolean = false,
    val equipTime: Long? = null,
    val usageCount: Int = 0,
    val lastUsedTime: Long? = null,
    val bondLevel: Int = 0,
    val bondExperience: Int = 0,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getUnlockStatus(): String {
        return if (isUnlocked) "Unlocked" else "Locked"
    }
    
    fun getFormattedUnlockTime(): String? {
        val timestamp = unlockTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceUnlock(): Long? {
        val timestamp = unlockTime ?: return null
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceUnlockMinutes(): Float? {
        return getTimeSinceUnlock()?.let { it / 60000f }
    }
    
    fun getUnlockSourceName(): String {
        return unlockSource?.name ?: "Unknown"
    }
    
    fun getFormattedEquipTime(): String? {
        val timestamp = equipTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceEquip(): Long? {
        val timestamp = equipTime ?: return null
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceEquipMinutes(): Float? {
        return getTimeSinceEquip()?.let { it / 60000f }
    }
    
    fun getFormattedLastUsedTime(): String? {
        val timestamp = lastUsedTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceLastUse(): Long? {
        val timestamp = lastUsedTime ?: return null
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceLastUseMinutes(): Float? {
        return getTimeSinceLastUse()?.let { it / 60000f }
    }
    
    fun getUsageFrequency(): Float {
        val timeSinceUnlock = getTimeSinceUnlock() ?: return 0f
        val timeSinceUnlockMinutes = timeSinceUnlock / 60000f
        return if (timeSinceUnlockMinutes > 0) {
            usageCount.toFloat() / timeSinceUnlockMinutes
        } else {
            0f
        }
    }
    
    fun getBondLevelProgress(): Float {
        return bondExperience.toFloat() / getExperienceForNextBondLevel()
    }
    
    fun getBondLevelProgressPercentage(): Int {
        return (getBondLevelProgress() * 100).toInt()
    }
    
    fun getExperienceForNextBondLevel(): Int {
        return bondLevel * 100
    }
    
    fun canLevelUpBond(): Boolean {
        return bondExperience >= getExperienceForNextBondLevel()
    }
    
    fun getMaxShardCount(): Int {
        return 999
    }
    
    fun getShardProgress(): Float {
        return shardCount.toFloat() / getMaxShardCount().toFloat()
    }
    
    fun getShardProgressPercentage(): Int {
        return (getShardProgress() * 100).toInt()
    }
    
    fun getShardsNeededForSynthesis(): Int {
        return 100 - shardCount
    }
    
    fun canSynthesize(): Boolean {
        return shardCount >= 100
    }
}

@Serializable
data class ShardState(
    val skinId: String,
    val count: Int,
    val acquireTime: Long = System.currentTimeMillis(),
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) {
    fun getFormattedAcquireTime(): String {
        val date = java.util.Date(acquireTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceAcquire(): Long {
        return System.currentTimeMillis() - acquireTime
    }
    
    fun getTimeSinceAcquireMinutes(): Float {
        return getTimeSinceAcquire() / 60000f
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
    
    fun getMaxCount(): Int {
        return 999
    }
    
    fun getRemainingCapacity(): Int {
        return (getMaxCount() - count).coerceAtLeast(0)
    }
    
    fun getCapacityPercentage(): Float {
        return count.toFloat() / getMaxCount().toFloat()
    }
    
    fun getCapacityPercentageInt(): Int {
        return (getCapacityPercentage() * 100).toInt()
    }
    
    fun isFull(): Boolean {
        return count >= getMaxCount()
    }
    
    fun isEmpty(): Boolean {
        return count <= 0
    }
}

@Serializable
enum class SkinUnlockSource {
    GACHA,
    SHARD_SYNTHESIS,
    SHOP,
    EVENT,
    ACHIEVEMENT,
    GIFT,
    DEFAULT
}

@Serializable
data class InventorySnapshot(
    val userId: String,
    val snapshotId: String,
    val inventory: Inventory,
    val snapshotTime: Long = System.currentTimeMillis(),
    val reason: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getFormattedSnapshotTime(): String {
        val date = java.util.Date(snapshotTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceSnapshot(): Long {
        return System.currentTimeMillis() - snapshotTime
    }
    
    fun getTimeSinceSnapshotMinutes(): Float {
        return getTimeSinceSnapshot() / 60000f
    }
}

@Serializable
data class InventoryChange(
    val userId: String,
    val changeType: InventoryChangeType,
    val itemType: ItemType,
    val itemId: String,
    val oldCount: Int,
    val newCount: Int,
    val changeAmount: Int,
    val changeTime: Long = System.currentTimeMillis(),
    val reason: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getFormattedChangeTime(): String {
        val date = java.util.Date(changeTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceChange(): Long {
        return System.currentTimeMillis() - changeTime
    }
    
    fun getTimeSinceChangeMinutes(): Float {
        return getTimeSinceChange() / 60000f
    }
    
    fun isAddition(): Boolean {
        return changeAmount > 0
    }
    
    fun isRemoval(): Boolean {
        return changeAmount < 0
    }
    
    fun getChangeDescription(): String {
        return when (changeType) {
            InventoryChangeType.ACQUIRE -> "Acquired"
            InventoryChangeType.USE -> "Used"
            InventoryChangeType.EQUIP -> "Equipped"
            InventoryChangeType.UNEQUIP -> "Unequipped"
            InventoryChangeType.SYNTHESIZE -> "Synthesized"
            InventoryChangeType.EXPIRE -> "Expired"
            InventoryChangeType.LOSS -> "Lost"
            InventoryChangeType.TRANSFER -> "Transferred"
            InventoryChangeType.REFUND -> "Refunded"
        }
    }
}

@Serializable
enum class InventoryChangeType {
    ACQUIRE,
    USE,
    EQUIP,
    UNEQUIP,
    SYNTHESIZE,
    EXPIRE,
    LOSS,
    TRANSFER,
    REFUND
}

@Serializable
enum class ItemType {
    SKILL_CARD,
    ITEM,
    SKIN,
    SHARD,
    CURRENCY,
    BUFF,
    CHAT_BUBBLE,
    EMOJI_PACK,
    AVATAR_FRAME,
    THEME
}
