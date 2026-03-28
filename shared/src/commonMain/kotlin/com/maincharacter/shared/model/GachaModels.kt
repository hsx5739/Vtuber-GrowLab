package com.maincharacter.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class GachaPoolDef(
    val poolId: String,
    val poolName: String,
    val poolDescription: String,
    val poolType: GachaPoolType,
    val cost: GachaCost,
    val rows: List<GachaRowDef>,
    val pityRules: PityRules,
    val duplicateRules: DuplicateRules,
    val probabilityDisplay: ProbabilityDisplay,
    val startTime: Long,
    val endTime: Long,
    val isActive: Boolean,
    val maxPullsPerUser: Int? = null,
    val featuredSkins: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
) {
    fun isAvailable(): Boolean {
        val currentTime = System.currentTimeMillis()
        return isActive && currentTime >= startTime && currentTime <= endTime
    }
    
    fun getRowByRarity(rarity: SkinRarity): GachaRowDef? {
        return rows.find { it.rarity == rarity }
    }
    
    fun getFeaturedSkinIdList(): List<String> {
        return featuredSkins
    }
    
    fun getTotalWeight(): Int {
        return rows.sumOf { it.weight }
    }
    
    fun getRarityWeight(rarity: SkinRarity): Int {
        return getRowByRarity(rarity)?.weight ?: 0
    }
}

@Serializable
data class GachaRowDef(
    val rowId: String,
    val rarity: SkinRarity,
    val weight: Int,
    val skinIds: List<String>,
    val guaranteedSkinId: String? = null,
    val featuredSkinIds: List<String> = emptyList(),
    val minGuaranteedCount: Int = 0,
    val maxGuaranteedCount: Int = Int.MAX_VALUE,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getAvailableSkinIds(): List<String> {
        return skinIds
    }
    
    fun getFeaturedSkinIdList(): List<String> {
        return featuredSkinIds
    }
    
    fun hasGuaranteedSkin(): Boolean {
        return guaranteedSkinId != null
    }
    
    fun getGuaranteedSkinIdValue(): String? {
        return guaranteedSkinId
    }
    
    fun isFeatured(skinId: String): Boolean {
        return skinId in featuredSkinIds
    }
    
    fun getMinGuaranteedCountValue(): Int {
        return minGuaranteedCount
    }
    
    fun getMaxGuaranteedCountValue(): Int {
        return maxGuaranteedCount
    }
}

@Serializable
data class GachaCost(
    val currencyType: CurrencyType,
    val singlePullCost: Int,
    val tenPullCost: Int,
    val discountTenPull: Boolean = true
) {
    fun calculateSinglePullCost(): Int {
        return singlePullCost
    }
    
    fun calculateTenPullCost(): Int {
        return if (discountTenPull) tenPullCost else singlePullCost * 10
    }
    
    fun hasDiscount(): Boolean {
        return discountTenPull && tenPullCost < singlePullCost * 10
    }
    
    fun getDiscountPercentage(): Float {
        if (!discountTenPull) return 0f
        
        val originalCost = singlePullCost * 10
        val discount = originalCost - tenPullCost
        
        return (discount.toFloat() / originalCost.toFloat()) * 100f
    }
}

@Serializable
data class PityRules(
    val rarePullsMax: Int,
    val rareRarity: SkinRarity,
    val superRarePullsMax: Int,
    val superRareRarity: SkinRarity,
    val ultraRarePullsMax: Int,
    val ultraRareRarity: SkinRarity,
    val featuredSkinPity: Int? = null,
    val resetAfterRare: Boolean = true,
    val resetAfterSuperRare: Boolean = true,
    val resetAfterUltraRare: Boolean = true
) {
    fun getRarityPity(rarity: SkinRarity): Int? {
        return when (rarity) {
            rareRarity -> rarePullsMax
            superRareRarity -> superRarePullsMax
            ultraRareRarity -> ultraRarePullsMax
            else -> null
        }
    }
    
    fun getFeaturedSkinPityValue(): Int? {
        return featuredSkinPity
    }
    
    fun shouldResetAfterRarity(rarity: SkinRarity): Boolean {
        return when (rarity) {
            rareRarity -> resetAfterRare
            superRareRarity -> resetAfterSuperRare
            ultraRareRarity -> resetAfterUltraRare
            else -> false
        }
    }
    
    fun getHighestRarity(): SkinRarity {
        return listOf(rareRarity, superRareRarity, ultraRareRarity)
            .maxByOrNull { it.tier } ?: SkinRarity.N
    }
}

@Serializable
data class DuplicateRules(
    val duplicateSkinToShard: Boolean = true,
    val shardPerDuplicate: Map<SkinRarity, Int> = mapOf(
        SkinRarity.N to 10,
        SkinRarity.R to 20,
        SkinRarity.SR to 50,
        SkinRarity.SSR to 100
    ),
    val maxShardPerSkin: Int = 999,
    val allowDuplicateConversion: Boolean = true
) {
    fun getShardCountForRarity(rarity: SkinRarity): Int {
        return shardPerDuplicate[rarity] ?: 0
    }
    
    fun canConvertDuplicate(): Boolean {
        return duplicateSkinToShard && allowDuplicateConversion
    }
    
    fun getMaxShardCount(): Int {
        return maxShardPerSkin
    }
    
    fun getShardForDuplicate(skinId: String, rarity: SkinRarity): Int {
        if (!canConvertDuplicate()) return 0
        
        return getShardCountForRarity(rarity)
    }
}

@Serializable
data class ProbabilityDisplay(
    val displayProbabilities: Map<SkinRarity, Float>,
    val featuredProbability: Float? = null,
    val guaranteedProbability: Float? = null,
    val displayFormat: ProbabilityDisplayFormat = ProbabilityDisplayFormat.PERCENTAGE,
    val showDetailedProbabilities: Boolean = false
) {
    fun getProbabilityForRarity(rarity: SkinRarity): Float? {
        return displayProbabilities[rarity]
    }
    
    fun getFeaturedProbabilityValue(): Float? {
        return featuredProbability
    }
    
    fun getGuaranteedProbabilityValue(): Float? {
        return guaranteedProbability
    }
    
    fun getProbabilityDisplayFormat(): ProbabilityDisplayFormat {
        return displayFormat
    }
    
    fun shouldShowDetailedProbabilities(): Boolean {
        return showDetailedProbabilities
    }
    
    fun getFormattedProbability(rarity: SkinRarity): String {
        val probability = getProbabilityForRarity(rarity) ?: return "0%"
        
        return when (displayFormat) {
            ProbabilityDisplayFormat.PERCENTAGE -> "${(probability * 100).toInt()}%"
            ProbabilityDisplayFormat.DECIMAL -> String.format("%.4f", probability)
            ProbabilityDisplayFormat.FRACTION -> "${(probability * 10000).toInt()}/10000"
        }
    }
}

@Serializable
enum class GachaPoolType {
    STANDARD,
    FEATURED,
    LIMITED,
    EVENT,
    COLLABORATION
}

@Serializable
enum class SkinRarity(val tier: Int) {
    N(1),
    R(2),
    SR(3),
    SSR(4)
}

@Serializable
enum class CurrencyType {
    STAR_DUST,
    MOON_GLOW,
    GACHA_TICKET,
    REAL_MONEY
}

@Serializable
enum class ProbabilityDisplayFormat {
    PERCENTAGE,
    DECIMAL,
    FRACTION
}

@Serializable
data class GachaResult(
    val poolId: String,
    val pullId: String,
    val skinId: String,
    val skinName: String,
    val rarity: SkinRarity,
    val isNew: Boolean,
    val isDuplicate: Boolean,
    val isFeatured: Boolean,
    val shardReward: Int? = null,
    val pityTriggered: Set<SkinRarity> = emptySet(),
    val pullCount: Int,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getRarityTier(): Int {
        return rarity.tier
    }
    
    fun isHighestRarity(): Boolean {
        return rarity == SkinRarity.SSR
    }
    
    fun isRareOrAbove(): Boolean {
        return rarity.tier >= SkinRarity.R.tier
    }
    
    fun isSuperRareOrAbove(): Boolean {
        return rarity.tier >= SkinRarity.SR.tier
    }
    
    fun getShardReward(): Int {
        return shardReward ?: 0
    }
    
    fun hasPityTriggered(): Boolean {
        return pityTriggered.isNotEmpty()
    }
    
    fun getPityTriggeredRarities(): List<SkinRarity> {
        return pityTriggered.toList()
    }
}

@Serializable
data class GachaPullSummary(
    val poolId: String,
    val pullCount: Int,
    val results: List<GachaResult>,
    val totalCost: Int,
    val currencyType: CurrencyType,
    val newSkins: Set<String>,
    val duplicateSkins: Set<String>,
    val totalShards: Int,
    val pityCounters: Map<SkinRarity, Int>,
    val timestamp: Long
) {
    fun getResultsByRarity(rarity: SkinRarity): List<GachaResult> {
        return results.filter { it.rarity == rarity }
    }
    
    fun getRarityCount(rarity: SkinRarity): Int {
        return results.count { it.rarity == rarity }
    }
    
    fun getNewSkinCount(): Int {
        return newSkins.size
    }
    
    fun getDuplicateSkinCount(): Int {
        return duplicateSkins.size
    }
    
    fun getFeaturedSkinCount(): Int {
        return results.count { it.isFeatured }
    }
    
    fun getHighestRarity(): SkinRarity? {
        return results.map { it.rarity }.maxByOrNull { it.tier }
    }
    
    fun hasSSR(): Boolean {
        return results.any { it.rarity == SkinRarity.SSR }
    }
    
    fun hasSR(): Boolean {
        return results.any { it.rarity == SkinRarity.SR }
    }
    
    fun hasRareOrAbove(): Boolean {
        return results.any { it.isRareOrAbove() }
    }
    
    fun getPityCounter(rarity: SkinRarity): Int {
        return pityCounters[rarity] ?: 0
    }
    
    fun calculateTotalCost(): Int {
        return totalCost
    }
    
    fun getCostPerPull(): Float {
        return totalCost.toFloat() / pullCount.toFloat()
    }
}

@Serializable
data class GachaHistory(
    val userId: String,
    val poolId: String,
    val pullId: String,
    val pullType: PullType,
    val pullCount: Int,
    val results: List<GachaResult>,
    val cost: GachaCost,
    val totalCost: Int,
    val pityCountersBefore: Map<SkinRarity, Int>,
    val pityCountersAfter: Map<SkinRarity, Int>,
    val timestamp: Long,
    val sessionId: String? = null,
    val deviceId: String? = null,
    val ipAddress: String? = null
) {
    fun getPullTypeValue(): PullType {
        return pullType
    }
    
    fun getPullCountValue(): Int {
        return pullCount
    }
    
    fun getResultList(): List<GachaResult> {
        return results
    }
    
    fun getCostValue(): GachaCost {
        return cost
    }
    
    fun calculateTotalCostValue(): Int {
        return totalCost
    }
    
    fun getPityCountersBefore(rarity: SkinRarity): Int {
        return pityCountersBefore[rarity] ?: 0
    }
    
    fun getPityCountersAfter(rarity: SkinRarity): Int {
        return pityCountersAfter[rarity] ?: 0
    }
    
    fun getPityIncrement(rarity: SkinRarity): Int {
        return getPityCountersAfter(rarity) - getPityCountersBefore(rarity)
    }
    
    fun getSessionIdValue(): String? {
        return sessionId
    }
    
    fun getDeviceIdValue(): String? {
        return deviceId
    }
    
    fun getIpAddressValue(): String? {
        return ipAddress
    }
    
    fun getTimestampValue(): Long {
        return timestamp
    }
    
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
enum class PullType {
    SINGLE,
    TEN
}
