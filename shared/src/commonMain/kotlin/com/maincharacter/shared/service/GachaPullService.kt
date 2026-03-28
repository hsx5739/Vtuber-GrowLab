package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlin.random.Random

class GachaPullService(
    private val poolConfig: GachaPoolConfig,
    private val currencyManager: GachaCurrencyManager,
    private val inventoryManager: GachaInventoryManager,
    private val pityManager: GachaPityManager,
    private val historyManager: GachaHistoryRecorder
) {
    
    private val _pullState = MutableStateFlow<PullState>(PullState.Idle)
    val pullState: StateFlow<PullState> = _pullState
    
    private val _currentPull = MutableStateFlow<CurrentPull?>(null)
    val currentPull: StateFlow<CurrentPull?> = _currentPull
    
    private val _pullStats = MutableStateFlow(PullStats())
    val pullStats: StateFlow<PullStats> = _pullStats
    
    fun pullSingle(poolId: String, userId: String): PullResult {
        return performPull(poolId, userId, PullType.SINGLE, 1)
    }
    
    fun pullTen(poolId: String, userId: String): PullResult {
        return performPull(poolId, userId, PullType.TEN, 10)
    }
    
    private fun performPull(
        poolId: String,
        userId: String,
        pullType: PullType,
        pullCount: Int
    ): PullResult {
        val pool = poolConfig.getPool(poolId)
        
        if (pool == null) {
            return PullResult(
                success = false,
                error = "Pool not found: $poolId",
                summary = null
            )
        }
        
        if (!pool.isAvailable()) {
            return PullResult(
                success = false,
                error = "Pool is not available",
                summary = null
            )
        }
        
        val cost = calculateCost(pool, pullType)
        
        val currencyCheck = currencyManager.checkCurrency(userId, pool.cost.currencyType, cost)
        
        if (!currencyCheck.hasEnough) {
            return PullResult(
                success = false,
                error = "Insufficient ${pool.cost.currencyType}",
                summary = null
            )
        }
        
        _pullState.value = PullState.Processing(
            poolId = poolId,
            pullType = pullType,
            pullCount = pullCount,
            startTime = System.currentTimeMillis()
        )
        
        val currentPull = CurrentPull(
            poolId = poolId,
            userId = userId,
            pullType = pullType,
            pullCount = pullCount,
            cost = cost,
            currencyType = pool.cost.currencyType,
            startTime = System.currentTimeMillis()
        )
        
        _currentPull.value = currentPull
        
        val currencyDeductResult = currencyManager.deductCurrency(
            userId,
            pool.cost.currencyType,
            cost
        )
        
        if (!currencyDeductResult.success) {
            _pullState.value = PullState.Failed(
                poolId = poolId,
                error = "Failed to deduct currency: ${currencyDeductResult.error}",
                endTime = System.currentTimeMillis()
            )
            
            _currentPull.value = null
            
            return PullResult(
                success = false,
                error = "Failed to deduct currency: ${currencyDeductResult.error}",
                summary = null
            )
        }
        
        val results = mutableListOf<GachaResult>()
        val pityCountersBefore = pityManager.getPityCounters(userId, poolId)
        
        for (i in 1..pullCount) {
            val result = performSinglePull(pool, userId, i, pullCount)
            results.add(result)
            
            pityManager.incrementPityCounter(userId, poolId, result.rarity)
        }
        
        val pityCountersAfter = pityManager.getPityCounters(userId, poolId)
        
        val newSkins = mutableSetOf<String>()
        val duplicateSkins = mutableSetOf<String>()
        var totalShards = 0
        
        results.forEach { result ->
            val isNew = inventoryManager.isNewSkin(userId, result.skinId)
            
            if (isNew) {
                inventoryManager.addSkin(userId, result.skinId)
                newSkins.add(result.skinId)
            } else {
                duplicateSkins.add(result.skinId)
                val shardReward = pool.duplicateRules.getShardForDuplicate(
                    result.skinId,
                    result.rarity
                )
                totalShards += shardReward
                inventoryManager.addShards(userId, result.skinId, shardReward)
            }
        }
        
        val summary = GachaPullSummary(
            poolId = poolId,
            pullCount = pullCount,
            results = results,
            totalCost = cost,
            currencyType = pool.cost.currencyType,
            newSkins = newSkins,
            duplicateSkins = duplicateSkins,
            totalShards = totalShards,
            pityCounters = pityCountersAfter,
            timestamp = System.currentTimeMillis()
        )
        
        val history = GachaHistory(
            userId = userId,
            poolId = poolId,
            pullId = generatePullId(),
            pullType = pullType,
            pullCount = pullCount,
            results = results,
            cost = pool.cost,
            totalCost = cost,
            pityCountersBefore = pityCountersBefore,
            pityCountersAfter = pityCountersAfter,
            timestamp = System.currentTimeMillis()
        )
        
        historyManager.recordPull(history)
        
        updateStats(summary)
        
        _pullState.value = PullState.Completed(
            poolId = poolId,
            summary = summary,
            endTime = System.currentTimeMillis()
        )
        
        _currentPull.value = null
        
        return PullResult(
            success = true,
            error = null,
            summary = summary
        )
    }
    
    private fun calculateCost(pool: GachaPoolDef, pullType: PullType): Int {
        return when (pullType) {
            PullType.SINGLE -> pool.cost.calculateSinglePullCost()
            PullType.TEN -> pool.cost.calculateTenPullCost()
        }
    }
    
    private fun performSinglePull(
        pool: GachaPoolDef,
        userId: String,
        pullIndex: Int,
        totalPulls: Int
    ): GachaResult {
        val pityCounters = pityManager.getPityCounters(userId, pool.poolId)
        
        val pityTriggered = mutableSetOf<SkinRarity>()
        
        val rarity = determineRarity(pool, pityCounters, pityTriggered)
        
        val row = pool.getRowByRarity(rarity) ?: pool.rows.first()
        
        val skinId = selectSkin(row, pool.featuredSkins)
        
        val isNew = inventoryManager.isNewSkin(userId, skinId)
        val isDuplicate = !isNew
        val isFeatured = pool.featuredSkins.contains(skinId)
        
        val shardReward = if (isDuplicate) {
            pool.duplicateRules.getShardForDuplicate(skinId, rarity)
        } else {
            null
        }
        
        return GachaResult(
            poolId = pool.poolId,
            pullId = generatePullId(),
            skinId = skinId,
            skinName = getSkinName(skinId),
            rarity = rarity,
            isNew = isNew,
            isDuplicate = isDuplicate,
            isFeatured = isFeatured,
            shardReward = shardReward,
            pityTriggered = pityTriggered,
            pullCount = totalPulls,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun determineRarity(
        pool: GachaPoolDef,
        pityCounters: Map<SkinRarity, Int>,
        pityTriggered: MutableSet<SkinRarity>
    ): SkinRarity {
        val pityRules = pool.pityRules
        
        val ultraRarePity = pityRules.getRarityPity(pityRules.ultraRareRarity)
        val superRarePity = pityRules.getRarityPity(pityRules.superRareRarity)
        val rarePity = pityRules.getRarityPity(pityRules.rareRarity)
        
        val ultraRareCounter = pityCounters[pityRules.ultraRareRarity] ?: 0
        val superRareCounter = pityCounters[pityRules.superRareRarity] ?: 0
        val rareCounter = pityCounters[pityRules.rareRarity] ?: 0
        
        when {
            ultraRarePity != null && ultraRareCounter >= ultraRarePity -> {
                pityTriggered.add(pityRules.ultraRareRarity)
                return pityRules.ultraRareRarity
            }
            superRarePity != null && superRareCounter >= superRarePity -> {
                pityTriggered.add(pityRules.superRareRarity)
                return pityRules.superRareRarity
            }
            rarePity != null && rareCounter >= rarePity -> {
                pityTriggered.add(pityRules.rareRarity)
                return pityRules.rareRarity
            }
        }
        
        val totalWeight = pool.getTotalWeight()
        val randomValue = Random.nextInt(1, totalWeight + 1)
        
        var currentWeight = 0
        for (row in pool.rows.sortedByDescending { it.rarity.tier }) {
            currentWeight += row.weight
            if (randomValue <= currentWeight) {
                return row.rarity
            }
        }
        
        return pool.rows.first().rarity
    }
    
    private fun selectSkin(row: GachaRowDef, featuredSkins: List<String>): String {
        val availableSkins = row.getAvailableSkinIds()
        
        if (row.hasGuaranteedSkin()) {
            return row.getGuaranteedSkinIdValue()!!
        }
        
        val featuredInRow = row.getFeaturedSkinIdList().filter { it in availableSkins }
        
        if (featuredInRow.isNotEmpty()) {
            val featuredWeight = 3
            val normalWeight = 1
            val totalWeight = featuredInRow.size * featuredWeight + (availableSkins.size - featuredInRow.size) * normalWeight
            
            val randomValue = Random.nextInt(1, totalWeight + 1)
            
            var currentWeight = 0
            for (skinId in availableSkins) {
                val weight = if (skinId in featuredInRow) featuredWeight else normalWeight
                currentWeight += weight
                if (randomValue <= currentWeight) {
                    return skinId
                }
            }
        }
        
        return availableSkins.random()
    }
    
    private fun getSkinName(skinId: String): String {
        return "Skin $skinId"
    }
    
    fun getPullState(): PullState {
        return _pullState.value
    }
    
    fun getCurrentPull(): CurrentPull? {
        return _currentPull.value
    }
    
    fun getPullStats(): PullStats {
        return _pullStats.value
    }
    
    fun cancelCurrentPull(): GachaCancelResult {
        val current = _currentPull.value
        
        if (current == null) {
            return GachaCancelResult(
                success = false,
                error = "No pull in progress"
            )
        }
        
        _pullState.value = PullState.Cancelled(
            poolId = current.poolId,
            endTime = System.currentTimeMillis()
        )
        
        _currentPull.value = null
        
        return GachaCancelResult(
            success = true,
            error = null
        )
    }
    
    private fun updateStats(summary: GachaPullSummary) {
        val current = _pullStats.value
        
        val updated = current.copy(
            totalPulls = current.totalPulls + summary.pullCount,
            totalCost = current.totalCost + summary.totalCost,
            totalSkinsObtained = current.totalSkinsObtained + summary.results.size,
            totalNewSkins = current.totalNewSkins + summary.newSkins.size,
            totalDuplicateSkins = current.totalDuplicateSkins + summary.duplicateSkins.size,
            totalShards = current.totalShards + summary.totalShards,
            ssrCount = current.ssrCount + summary.getRarityCount(SkinRarity.SSR),
            srCount = current.srCount + summary.getRarityCount(SkinRarity.SR),
            rCount = current.rCount + summary.getRarityCount(SkinRarity.R),
            nCount = current.nCount + summary.getRarityCount(SkinRarity.N)
        )
        
        _pullStats.value = updated
    }
    
    private fun generatePullId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "pull_${timestamp}_$random"
    }
}

sealed class PullState {
    data object Idle : PullState()
    
    data class Processing(
        val poolId: String,
        val pullType: PullType,
        val pullCount: Int,
        val startTime: Long
    ) : PullState()
    
    data class Completed(
        val poolId: String,
        val summary: GachaPullSummary,
        val endTime: Long
    ) : PullState()
    
    data class Failed(
        val poolId: String,
        val error: String,
        val endTime: Long
    ) : PullState()
    
    data class Cancelled(
        val poolId: String,
        val endTime: Long
    ) : PullState()
}

@Serializable
data class CurrentPull(
    val poolId: String,
    val userId: String,
    val pullType: PullType,
    val pullCount: Int,
    val cost: Int,
    val currencyType: CurrencyType,
    val startTime: Long
) {
    fun getElapsedTime(): Long {
        return System.currentTimeMillis() - startTime
    }
    
    fun getElapsedTimeSeconds(): Float {
        return getElapsedTime() / 1000f
    }
}

@Serializable
data class PullStats(
    val totalPulls: Int = 0,
    val totalCost: Int = 0,
    val totalSkinsObtained: Int = 0,
    val totalNewSkins: Int = 0,
    val totalDuplicateSkins: Int = 0,
    val totalShards: Int = 0,
    val ssrCount: Int = 0,
    val srCount: Int = 0,
    val rCount: Int = 0,
    val nCount: Int = 0
) {
    fun getAverageCostPerPull(): Float {
        return if (totalPulls > 0) {
            totalCost.toFloat() / totalPulls.toFloat()
        } else {
            0f
        }
    }
    
    fun getSSRRate(): Float {
        return if (totalPulls > 0) {
            (ssrCount.toFloat() / totalPulls.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getSRRate(): Float {
        return if (totalPulls > 0) {
            (srCount.toFloat() / totalPulls.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getRRate(): Float {
        return if (totalPulls > 0) {
            (rCount.toFloat() / totalPulls.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getNRate(): Float {
        return if (totalPulls > 0) {
            (nCount.toFloat() / totalPulls.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getNewSkinRate(): Float {
        return if (totalSkinsObtained > 0) {
            (totalNewSkins.toFloat() / totalSkinsObtained.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getDuplicateSkinRate(): Float {
        return if (totalSkinsObtained > 0) {
            (totalDuplicateSkins.toFloat() / totalSkinsObtained.toFloat()) * 100f
        } else {
            0f
        }
    }
}

@Serializable
data class PullResult(
    val success: Boolean,
    val error: String?,
    val summary: GachaPullSummary?
) {
    fun isSuccessful(): Boolean {
        return success
    }
    
    fun getErrorMessage(): String? {
        return error
    }
    
    fun getPullSummary(): GachaPullSummary? {
        return summary
    }
}

@Serializable
data class GachaCancelResult(
    val success: Boolean,
    val error: String?
) {
    fun isSuccessful(): Boolean {
        return success
    }
    
    fun getErrorMessage(): String? {
        return error
    }
}

interface GachaCurrencyManager {
    fun checkCurrency(userId: String, currencyType: CurrencyType, amount: Int): CurrencyCheckResult
    fun deductCurrency(userId: String, currencyType: CurrencyType, amount: Int): CurrencyDeductResult
}

interface GachaInventoryManager {
    fun isNewSkin(userId: String, skinId: String): Boolean
    fun addSkin(userId: String, skinId: String)
    fun addShards(userId: String, skinId: String, amount: Int)
}

interface GachaPityManager {
    fun getPityCounters(userId: String, poolId: String): Map<SkinRarity, Int>
    fun incrementPityCounter(userId: String, poolId: String, rarity: SkinRarity)
}

interface GachaHistoryRecorder {
    fun recordPull(history: GachaHistory)
}

@Serializable
data class CurrencyCheckResult(
    val hasEnough: Boolean,
    val currentAmount: Int,
    val requiredAmount: Int
)

@Serializable
data class CurrencyDeductResult(
    val success: Boolean,
    val error: String?,
    val newAmount: Int
)
