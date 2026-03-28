package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable

class ProbabilityDisclosureService(
    private val poolConfig: GachaPoolConfig
) {
    
    private val _disclosureData = MutableStateFlow<Map<String, ProbabilityDisclosure>>(emptyMap())
    val disclosureData: StateFlow<Map<String, ProbabilityDisclosure>> = _disclosureData
    
    private val _disclosureHistory = MutableStateFlow<List<DisclosureHistoryEntry>>(emptyList())
    val disclosureHistory: StateFlow<List<DisclosureHistoryEntry>> = _disclosureHistory
    
    private val _disclosureStats = MutableStateFlow(DisclosureStats())
    val disclosureStats: StateFlow<DisclosureStats> = _disclosureStats
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    fun generateDisclosure(poolId: String): ProbabilityDisclosure? {
        val pool = poolConfig.getPool(poolId) ?: return null
        
        val rarityProbabilities = calculateRarityProbabilities(pool)
        
        val featuredProbabilities = calculateFeaturedProbabilities(pool)
        
        val pityInformation = generatePityInformation(pool)
        
        val duplicateRules = generateDuplicateRulesInformation(pool)
        
        val costInformation = generateCostInformation(pool)
        
        val poolInformation = PoolInformation(
            poolId = pool.poolId,
            poolName = pool.poolName,
            poolType = pool.poolType,
            startTime = pool.startTime,
            endTime = pool.endTime,
            isActive = pool.isAvailable()
        )
        
        val disclosure = ProbabilityDisclosure(
            poolId = poolId,
            poolInformation = poolInformation,
            rarityProbabilities = rarityProbabilities,
            featuredProbabilities = featuredProbabilities,
            pityInformation = pityInformation,
            duplicateRules = duplicateRules,
            costInformation = costInformation,
            lastUpdated = System.currentTimeMillis(),
            version = poolConfig.getConfigVersion()
        )
        
        val updatedData = _disclosureData.value.toMutableMap()
        updatedData[poolId] = disclosure
        _disclosureData.value = updatedData
        
        recordDisclosureHistory(disclosure)
        
        updateStats(disclosure)
        
        return disclosure
    }
    
    private fun calculateRarityProbabilities(pool: GachaPoolDef): Map<SkinRarity, RarityProbability> {
        val totalWeight = pool.getTotalWeight()
        
        return pool.rows.associate { row ->
            val probability = if (totalWeight > 0) {
                row.weight.toFloat() / totalWeight.toFloat()
            } else {
                0f
            }
            
            row.rarity to RarityProbability(
                rarity = row.rarity,
                probability = probability,
                weight = row.weight,
                skinCount = row.skinIds.size,
                guaranteedSkinId = row.guaranteedSkinId,
                featuredSkinIds = row.featuredSkinIds
            )
        }
    }
    
    private fun calculateFeaturedProbabilities(pool: GachaPoolDef): Map<String, FeaturedProbability> {
        val featuredProbabilities = mutableMapOf<String, FeaturedProbability>()
        
        pool.featuredSkins.forEach { skinId ->
            val skin = findSkinInPool(pool, skinId)
            
            if (skin != null) {
                val row = pool.getRowByRarity(skin.rarity)
                val totalWeight = pool.getTotalWeight()
                
                val baseProbability = if (row != null && totalWeight > 0) {
                    row.weight.toFloat() / totalWeight.toFloat()
                } else {
                    0f
                }
                
                val featuredBonus = 0.5f
                val featuredProbability = baseProbability * (1f + featuredBonus)
                
                featuredProbabilities[skinId] = FeaturedProbability(
                    skinId = skinId,
                    skinName = skin.name,
                    rarity = skin.rarity,
                    baseProbability = baseProbability,
                    featuredBonus = featuredBonus,
                    featuredProbability = featuredProbability
                )
            }
        }
        
        return featuredProbabilities
    }
    
    private fun findSkinInPool(pool: GachaPoolDef, skinId: String): SkinInfo? {
        for (row in pool.rows) {
            if (skinId in row.skinIds) {
                val isFeatured = skinId in row.featuredSkinIds
                val isGuaranteed = skinId == row.guaranteedSkinId
                
                return SkinInfo(
                    skinId = skinId,
                    name = "Skin $skinId",
                    rarity = row.rarity,
                    isFeatured = isFeatured,
                    isGuaranteed = isGuaranteed
                )
            }
        }
        
        return null
    }
    
    private fun generatePityInformation(pool: GachaPoolDef): PityInformation {
        val pityRules = pool.pityRules
        
        val rarePity = PityThreshold(
            rarity = pityRules.rareRarity,
            maxPulls = pityRules.rarePullsMax,
            guaranteed = true,
            description = "Guaranteed ${pityRules.rareRarity.name} within ${pityRules.rarePullsMax} pulls"
        )
        
        val superRarePity = PityThreshold(
            rarity = pityRules.superRareRarity,
            maxPulls = pityRules.superRarePullsMax,
            guaranteed = true,
            description = "Guaranteed ${pityRules.superRareRarity.name} within ${pityRules.superRarePullsMax} pulls"
        )
        
        val ultraRarePity = PityThreshold(
            rarity = pityRules.ultraRareRarity,
            maxPulls = pityRules.ultraRarePullsMax,
            guaranteed = true,
            description = "Guaranteed ${pityRules.ultraRareRarity.name} within ${pityRules.ultraRarePullsMax} pulls"
        )
        
        val featuredPity = pityRules.featuredSkinPity?.let {
            PityThreshold(
                rarity = null,
                maxPulls = it,
                guaranteed = false,
                description = "Increased chance for featured skin every $it pulls"
            )
        }
        
        return PityInformation(
            rarePity = rarePity,
            superRarePity = superRarePity,
            ultraRarePity = ultraRarePity,
            featuredPity = featuredPity,
            resetBehavior = PityResetBehavior(
                resetAfterRare = pityRules.resetAfterRare,
                resetAfterSuperRare = pityRules.resetAfterSuperRare,
                resetAfterUltraRare = pityRules.resetAfterUltraRare
            )
        )
    }
    
    private fun generateDuplicateRulesInformation(pool: GachaPoolDef): DuplicateRulesInformation {
        val duplicateRules = pool.duplicateRules
        
        val shardRewards = duplicateRules.shardPerDuplicate.map { (rarity, shards) ->
            ShardReward(
                rarity = rarity,
                shardsPerDuplicate = shards
            )
        }
        
        return DuplicateRulesInformation(
            canConvertDuplicates = duplicateRules.duplicateSkinToShard,
            shardRewards = shardRewards,
            maxShardsPerSkin = duplicateRules.maxShardPerSkin,
            allowDuplicateConversion = duplicateRules.allowDuplicateConversion
        )
    }
    
    private fun generateCostInformation(pool: GachaPoolDef): CostInformation {
        val cost = pool.cost
        
        return CostInformation(
            currencyType = cost.currencyType,
            singlePullCost = cost.singlePullCost,
            tenPullCost = cost.tenPullCost,
            hasDiscount = cost.hasDiscount(),
            discountPercentage = cost.getDiscountPercentage()
        )
    }
    
    fun getDisclosure(poolId: String): ProbabilityDisclosure? {
        return _disclosureData.value[poolId]
    }
    
    fun getAllDisclosures(): Map<String, ProbabilityDisclosure> {
        return _disclosureData.value
    }
    
    fun getRarityProbability(poolId: String, rarity: SkinRarity): RarityProbability? {
        val disclosure = getDisclosure(poolId) ?: return null
        return disclosure.rarityProbabilities[rarity]
    }
    
    fun getFeaturedProbability(poolId: String, skinId: String): FeaturedProbability? {
        val disclosure = getDisclosure(poolId) ?: return null
        return disclosure.featuredProbabilities[skinId]
    }
    
    fun getPityInformation(poolId: String): PityInformation? {
        val disclosure = getDisclosure(poolId) ?: return null
        return disclosure.pityInformation
    }
    
    fun getDuplicateRulesInformation(poolId: String): DuplicateRulesInformation? {
        val disclosure = getDisclosure(poolId) ?: return null
        return disclosure.duplicateRules
    }
    
    fun getCostInformation(poolId: String): CostInformation? {
        val disclosure = getDisclosure(poolId) ?: return null
        return disclosure.costInformation
    }
    
    fun generateAllDisclosures(): Map<String, ProbabilityDisclosure> {
        val pools = poolConfig.getAllPools()
        val disclosures = mutableMapOf<String, ProbabilityDisclosure>()
        
        pools.forEach { (poolId, _) ->
            val disclosure = generateDisclosure(poolId)
            if (disclosure != null) {
                disclosures[poolId] = disclosure
            }
        }
        
        return disclosures
    }
    
    fun refreshDisclosure(poolId: String): ProbabilityDisclosure? {
        return generateDisclosure(poolId)
    }
    
    fun refreshAllDisclosures(): Map<String, ProbabilityDisclosure> {
        return generateAllDisclosures()
    }
    
    fun exportDisclosure(poolId: String): String? {
        val disclosure = getDisclosure(poolId) ?: return null
        return json.encodeToString(disclosure)
    }
    
    fun exportAllDisclosures(): String {
        val disclosures = getAllDisclosures()
        return json.encodeToString(disclosures)
    }
    
    fun exportDisclosureAsText(poolId: String): String? {
        val disclosure = getDisclosure(poolId) ?: return null
        
        val text = buildString {
            appendLine("=== Probability Disclosure ===")
            appendLine()
            appendLine("Pool: ${disclosure.poolInformation.poolName}")
            appendLine("Type: ${disclosure.poolInformation.poolType}")
            appendLine("Status: ${if (disclosure.poolInformation.isActive) "Active" else "Inactive"}")
            appendLine()
            
            appendLine("--- Rarity Probabilities ---")
            disclosure.rarityProbabilities.values.forEach { prob ->
                appendLine("${prob.rarity.name}: ${(prob.probability * 100).toInt()}% (Weight: ${prob.weight})")
            }
            appendLine()
            
            if (disclosure.featuredProbabilities.isNotEmpty()) {
                appendLine("--- Featured Skins ---")
                disclosure.featuredProbabilities.values.forEach { prob ->
                    appendLine("${prob.skinName}: ${(prob.featuredProbability * 100).toInt()}% (Base: ${(prob.baseProbability * 100).toInt()}%)")
                }
                appendLine()
            }
            
            appendLine("--- Pity System ---")
            appendLine(disclosure.pityInformation.rarePity.description)
            appendLine(disclosure.pityInformation.superRarePity.description)
            appendLine(disclosure.pityInformation.ultraRarePity.description)
            disclosure.pityInformation.featuredPity?.let {
                appendLine(it.description)
            }
            appendLine()
            
            appendLine("--- Duplicate Rules ---")
            if (disclosure.duplicateRules.canConvertDuplicates) {
                appendLine("Duplicate skins can be converted to shards:")
                disclosure.duplicateRules.shardRewards.forEach { reward ->
                    appendLine("  ${reward.rarity.name}: ${reward.shardsPerDuplicate} shards")
                }
                appendLine("Max shards per skin: ${disclosure.duplicateRules.maxShardsPerSkin}")
            } else {
                appendLine("Duplicate skin conversion is disabled")
            }
            appendLine()
            
            appendLine("--- Cost ---")
            appendLine("Single Pull: ${disclosure.costInformation.singlePullCost} ${disclosure.costInformation.currencyType}")
            appendLine("Ten Pull: ${disclosure.costInformation.tenPullCost} ${disclosure.costInformation.currencyType}")
            if (disclosure.costInformation.hasDiscount) {
                appendLine("Discount: ${disclosure.costInformation.discountPercentage.toInt()}% off on ten pulls")
            }
            appendLine()
            
            appendLine("--- Last Updated ---")
            appendLine(disclosure.getFormattedLastUpdated())
        }
        
        return text
    }
    
    fun getDisclosureHistory(poolId: String? = null): List<DisclosureHistoryEntry> {
        var history = _disclosureHistory.value
        
        if (poolId != null) {
            history = history.filter { it.poolId == poolId }
        }
        
        return history
    }
    
    fun getRecentDisclosureHistory(limit: Int = 10): List<DisclosureHistoryEntry> {
        return _disclosureHistory.value.takeLast(limit)
    }
    
    fun getDisclosureStats(): DisclosureStats {
        return _disclosureStats.value
    }
    
    fun clearDisclosureData(poolId: String) {
        val updatedData = _disclosureData.value.toMutableMap()
        updatedData.remove(poolId)
        _disclosureData.value = updatedData
    }
    
    fun clearAllDisclosureData() {
        _disclosureData.value = emptyMap()
        _disclosureHistory.value = emptyList()
        _disclosureStats.value = DisclosureStats()
    }
    
    private fun recordDisclosureHistory(disclosure: ProbabilityDisclosure) {
        val entry = DisclosureHistoryEntry(
            id = generateHistoryId(),
            poolId = disclosure.poolId,
            version = disclosure.version,
            timestamp = disclosure.lastUpdated
        )
        
        val updatedHistory = _disclosureHistory.value.toMutableList()
        updatedHistory.add(entry)
        _disclosureHistory.value = updatedHistory
    }
    
    private fun updateStats(disclosure: ProbabilityDisclosure) {
        val current = _disclosureStats.value
        
        val updated = current.copy(
            totalDisclosures = current.totalDisclosures + 1,
            poolDisclosures = current.poolDisclosures.toMutableMap().apply {
                this[disclosure.poolId] = (this[disclosure.poolId] ?: 0) + 1
            },
            lastUpdateTime = disclosure.lastUpdated
        )
        
        _disclosureStats.value = updated
    }
    
    private fun generateHistoryId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "disclosure_history_${timestamp}_$random"
    }
}

@Serializable
data class ProbabilityDisclosure(
    val poolId: String,
    val poolInformation: PoolInformation,
    val rarityProbabilities: Map<SkinRarity, RarityProbability>,
    val featuredProbabilities: Map<String, FeaturedProbability>,
    val pityInformation: PityInformation,
    val duplicateRules: DuplicateRulesInformation,
    val costInformation: CostInformation,
    val lastUpdated: Long,
    val version: String
) {
    fun getFormattedLastUpdated(): String {
        val date = java.util.Date(lastUpdated)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceUpdate(): Long {
        return System.currentTimeMillis() - lastUpdated
    }
    
    fun getTimeSinceUpdateMinutes(): Float {
        return getTimeSinceUpdate() / 60000f
    }
}

@Serializable
data class PoolInformation(
    val poolId: String,
    val poolName: String,
    val poolType: GachaPoolType,
    val startTime: Long,
    val endTime: Long,
    val isActive: Boolean
) {
    fun getFormattedStartTime(): String {
        val date = java.util.Date(startTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getFormattedEndTime(): String {
        val date = java.util.Date(endTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
data class RarityProbability(
    val rarity: SkinRarity,
    val probability: Float,
    val weight: Int,
    val skinCount: Int,
    val guaranteedSkinId: String?,
    val featuredSkinIds: List<String>
) {
    fun getProbabilityPercentage(): Int {
        return (probability * 100).toInt()
    }
    
    fun getProbabilityDecimal(): String {
        return String.format("%.4f", probability)
    }
    
    fun getProbabilityFraction(): String {
        return "/${(1f / probability).toInt()}"
    }
}

@Serializable
data class FeaturedProbability(
    val skinId: String,
    val skinName: String,
    val rarity: SkinRarity,
    val baseProbability: Float,
    val featuredBonus: Float,
    val featuredProbability: Float
) {
    fun getBaseProbabilityPercentage(): Int {
        return (baseProbability * 100).toInt()
    }
    
    fun getFeaturedProbabilityPercentage(): Int {
        return (featuredProbability * 100).toInt()
    }
    
    fun getBonusPercentage(): Int {
        return (featuredBonus * 100).toInt()
    }
}

@Serializable
data class PityInformation(
    val rarePity: PityThreshold,
    val superRarePity: PityThreshold,
    val ultraRarePity: PityThreshold,
    val featuredPity: PityThreshold?,
    val resetBehavior: PityResetBehavior
)

@Serializable
data class PityThreshold(
    val rarity: SkinRarity?,
    val maxPulls: Int,
    val guaranteed: Boolean,
    val description: String
)

@Serializable
data class PityResetBehavior(
    val resetAfterRare: Boolean,
    val resetAfterSuperRare: Boolean,
    val resetAfterUltraRare: Boolean
)

@Serializable
data class DuplicateRulesInformation(
    val canConvertDuplicates: Boolean,
    val shardRewards: List<ShardReward>,
    val maxShardsPerSkin: Int,
    val allowDuplicateConversion: Boolean
)

@Serializable
data class ShardReward(
    val rarity: SkinRarity,
    val shardsPerDuplicate: Int
)

@Serializable
data class CostInformation(
    val currencyType: CurrencyType,
    val singlePullCost: Int,
    val tenPullCost: Int,
    val hasDiscount: Boolean,
    val discountPercentage: Float
)

@Serializable
data class DisclosureHistoryEntry(
    val id: String,
    val poolId: String,
    val version: String,
    val timestamp: Long
) {
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
data class DisclosureStats(
    val totalDisclosures: Int = 0,
    val poolDisclosures: Map<String, Int> = emptyMap(),
    val lastUpdateTime: Long = 0L
) {
    fun getPoolDisclosureCount(poolId: String): Int {
        return poolDisclosures[poolId] ?: 0
    }
    
    fun getMostUpdatedPool(): String? {
        return poolDisclosures.maxByOrNull { it.value }?.key
    }
    
    fun getFormattedLastUpdateTime(): String {
        val date = java.util.Date(lastUpdateTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
data class SkinInfo(
    val skinId: String,
    val name: String,
    val rarity: SkinRarity,
    val isFeatured: Boolean,
    val isGuaranteed: Boolean
)
