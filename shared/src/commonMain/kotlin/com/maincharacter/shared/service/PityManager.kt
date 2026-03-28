package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable

class PityManager(
    private val poolConfig: GachaPoolConfig
) {
    
    private val _pityCounters = MutableStateFlow<Map<String, Map<SkinRarity, Int>>>(emptyMap())
    val pityCounters: StateFlow<Map<String, Map<SkinRarity, Int>>> = _pityCounters
    
    private val _pityStates = MutableStateFlow<Map<String, PityState>>(emptyMap())
    val pityStates: StateFlow<Map<String, PityState>> = _pityStates
    
    private val _pityHistory = MutableStateFlow<List<PityEvent>>(emptyList())
    val pityHistory: StateFlow<List<PityEvent>> = _pityHistory
    
    private val _pityStats = MutableStateFlow(PityStats())
    val pityStats: StateFlow<PityStats> = _pityStats
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    fun getPityCounters(userId: String, poolId: String): Map<SkinRarity, Int> {
        val key = getPityKey(userId, poolId)
        return _pityCounters.value[key] ?: initializePityCounters(poolId)
    }
    
    fun initializePityCounters(poolId: String): Map<SkinRarity, Int> {
        val pool = poolConfig.getPool(poolId) ?: return emptyMap()
        
        val counters = mutableMapOf<SkinRarity, Int>()
        
        counters[pool.pityRules.rareRarity] = 0
        counters[pool.pityRules.superRareRarity] = 0
        counters[pool.pityRules.ultraRareRarity] = 0
        
        return counters
    }
    
    fun incrementPityCounter(userId: String, poolId: String, rarity: SkinRarity) {
        val pool = poolConfig.getPool(poolId) ?: return
        
        val key = getPityKey(userId, poolId)
        val currentCounters = _pityCounters.value[key] ?: initializePityCounters(poolId)
        
        val updatedCounters = currentCounters.toMutableMap()
        
        val pityRules = pool.pityRules
        
        val shouldReset = pityRules.shouldResetAfterRarity(rarity)
        
        if (shouldReset) {
            updatedCounters[pityRules.rareRarity] = 0
            updatedCounters[pityRules.superRareRarity] = 0
            updatedCounters[pityRules.ultraRareRarity] = 0
            
            recordPityEvent(userId, poolId, rarity, PityEventType.RESET)
        } else {
            val rarities = listOf(
                pityRules.rareRarity,
                pityRules.superRareRarity,
                pityRules.ultraRareRarity
            )
            
            rarities.forEach { r ->
                if (r.tier < rarity.tier) {
                    updatedCounters[r] = (updatedCounters[r] ?: 0) + 1
                }
            }
        }
        
        _pityCounters.value = _pityCounters.value.toMutableMap().apply {
            this[key] = updatedCounters
        }
        
        updatePityState(userId, poolId, updatedCounters)
        updateStats(rarity, shouldReset)
    }
    
    fun getPityState(userId: String, poolId: String): PityState {
        val key = getPityKey(userId, poolId)
        return _pityStates.value[key] ?: PityState(
            userId = userId,
            poolId = poolId,
            counters = emptyMap(),
            isPityTriggered = false,
            triggeredRarity = null,
            pullsUntilPity = emptyMap(),
            lastUpdateTime = 0L
        )
    }
    
    private fun updatePityState(userId: String, poolId: String, counters: Map<SkinRarity, Int>) {
        val pool = poolConfig.getPool(poolId) ?: return
        
        val pityRules = pool.pityRules
        
        val pullsUntilPity = mutableMapOf<SkinRarity, Int>()
        
        pullsUntilPity[pityRules.rareRarity] = (pityRules.rarePullsMax - (counters[pityRules.rareRarity] ?: 0)).coerceAtLeast(0)
        pullsUntilPity[pityRules.superRareRarity] = (pityRules.superRarePullsMax - (counters[pityRules.superRareRarity] ?: 0)).coerceAtLeast(0)
        pullsUntilPity[pityRules.ultraRareRarity] = (pityRules.ultraRarePullsMax - (counters[pityRules.ultraRareRarity] ?: 0)).coerceAtLeast(0)
        
        val isPityTriggered = pullsUntilPity.values.any { it == 0 }
        
        val triggeredRarity = if (isPityTriggered) {
            pullsUntilPity.entries.find { it.value == 0 }?.key
        } else {
            null
        }
        
        val pityState = PityState(
            userId = userId,
            poolId = poolId,
            counters = counters,
            isPityTriggered = isPityTriggered,
            triggeredRarity = triggeredRarity,
            pullsUntilPity = pullsUntilPity,
            lastUpdateTime = System.currentTimeMillis()
        )
        
        _pityStates.value = _pityStates.value.toMutableMap().apply {
            this[getPityKey(userId, poolId)] = pityState
        }
    }
    
    fun getPullsUntilPity(userId: String, poolId: String, rarity: SkinRarity): Int? {
        val pityState = getPityState(userId, poolId)
        return pityState.pullsUntilPity[rarity]
    }
    
    fun isPityTriggered(userId: String, poolId: String): Boolean {
        val pityState = getPityState(userId, poolId)
        return pityState.isPityTriggered
    }
    
    fun getTriggeredRarity(userId: String, poolId: String): SkinRarity? {
        val pityState = getPityState(userId, poolId)
        return pityState.triggeredRarity
    }
    
    fun resetPityCounters(userId: String, poolId: String) {
        val key = getPityKey(userId, poolId)
        val counters = initializePityCounters(poolId)
        
        _pityCounters.value = _pityCounters.value.toMutableMap().apply {
            this[key] = counters
        }
        
        updatePityState(userId, poolId, counters)
        
        recordPityEvent(userId, poolId, null, PityEventType.MANUAL_RESET)
    }
    
    fun resetPityCounter(userId: String, poolId: String, rarity: SkinRarity) {
        val key = getPityKey(userId, poolId)
        val currentCounters = _pityCounters.value[key] ?: initializePityCounters(poolId)
        
        val updatedCounters = currentCounters.toMutableMap()
        updatedCounters[rarity] = 0
        
        _pityCounters.value = _pityCounters.value.toMutableMap().apply {
            this[key] = updatedCounters
        }
        
        updatePityState(userId, poolId, updatedCounters)
        
        recordPityEvent(userId, poolId, rarity, PityEventType.SPECIFIC_RESET)
    }
    
    fun recordPityEvent(userId: String, poolId: String, rarity: SkinRarity?, eventType: PityEventType) {
        val event = PityEvent(
            id = generateEventId(),
            userId = userId,
            poolId = poolId,
            rarity = rarity,
            eventType = eventType,
            countersBefore = getPityCounters(userId, poolId),
            timestamp = System.currentTimeMillis()
        )
        
        val updatedHistory = _pityHistory.value.toMutableList()
        updatedHistory.add(event)
        _pityHistory.value = updatedHistory
    }
    
    fun getPityHistory(userId: String? = null, poolId: String? = null): List<PityEvent> {
        var history = _pityHistory.value
        
        if (userId != null) {
            history = history.filter { it.userId == userId }
        }
        
        if (poolId != null) {
            history = history.filter { it.poolId == poolId }
        }
        
        return history
    }
    
    fun getRecentPityEvents(limit: Int = 10): List<PityEvent> {
        return _pityHistory.value.takeLast(limit)
    }
    
    fun getPityStats(): PityStats {
        return _pityStats.value
    }
    
    fun getPitySummary(userId: String, poolId: String): PitySummary {
        val pityState = getPityState(userId, poolId)
        val pool = poolConfig.getPool(poolId)
        
        val totalPulls = pityState.counters.values.sum()
        
        val rareCounter = pityState.counters[pool?.pityRules?.rareRarity] ?: 0
        val superRareCounter = pityState.counters[pool?.pityRules?.superRareRarity] ?: 0
        val ultraRareCounter = pityState.counters[pool?.pityRules?.ultraRareRarity] ?: 0
        
        val rareProgress = if (pool != null) {
            (rareCounter.toFloat() / pool.pityRules.rarePullsMax.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        
        val superRareProgress = if (pool != null) {
            (superRareCounter.toFloat() / pool.pityRules.superRarePullsMax.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        
        val ultraRareProgress = if (pool != null) {
            (ultraRareCounter.toFloat() / pool.pityRules.ultraRarePullsMax.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        
        return PitySummary(
            userId = userId,
            poolId = poolId,
            totalPulls = totalPulls,
            rareCounter = rareCounter,
            superRareCounter = superRareCounter,
            ultraRareCounter = ultraRareCounter,
            rareProgress = rareProgress,
            superRareProgress = superRareProgress,
            ultraRareProgress = ultraRareProgress,
            isPityTriggered = pityState.isPityTriggered,
            triggeredRarity = pityState.triggeredRarity,
            lastUpdateTime = pityState.lastUpdateTime
        )
    }
    
    fun exportPityData(userId: String): String {
        val data = PityExportData(
            userId = userId,
            pityCounters = _pityCounters.value.filterKeys { it.startsWith("$userId:") },
            pityStates = _pityStates.value.filterKeys { it.startsWith("$userId:") },
            pityHistory = _pityHistory.value.filter { it.userId == userId },
            exportTime = System.currentTimeMillis()
        )
        
        return json.encodeToString(data)
    }
    
    fun clearPityData(userId: String) {
        val keysToRemove = _pityCounters.value.keys.filter { it.startsWith("$userId:") }
        
        val updatedCounters = _pityCounters.value.toMutableMap()
        keysToRemove.forEach { updatedCounters.remove(it) }
        _pityCounters.value = updatedCounters
        
        val updatedStates = _pityStates.value.toMutableMap()
        keysToRemove.forEach { updatedStates.remove(it) }
        _pityStates.value = updatedStates
        
        val updatedHistory = _pityHistory.value.filter { it.userId != userId }
        _pityHistory.value = updatedHistory
    }
    
    private fun updateStats(rarity: SkinRarity, wasReset: Boolean) {
        val current = _pityStats.value
        
        val updated = current.copy(
            totalPityEvents = current.totalPityEvents + 1,
            rarityEvents = current.rarityEvents.toMutableMap().apply {
                this[rarity] = (this[rarity] ?: 0) + 1
            },
            resetEvents = if (wasReset) {
                current.resetEvents + 1
            } else {
                current.resetEvents
            }
        )
        
        _pityStats.value = updated
    }
    
    private fun getPityKey(userId: String, poolId: String): String {
        return "$userId:$poolId"
    }
    
    private fun generateEventId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "pity_event_${timestamp}_$random"
    }
}

@Serializable
data class PityState(
    val userId: String,
    val poolId: String,
    val counters: Map<SkinRarity, Int>,
    val isPityTriggered: Boolean,
    val triggeredRarity: SkinRarity?,
    val pullsUntilPity: Map<SkinRarity, Int>,
    val lastUpdateTime: Long
) {
    fun getCounter(rarity: SkinRarity): Int {
        return counters[rarity] ?: 0
    }
    
    fun getPullsUntilPity(rarity: SkinRarity): Int? {
        return pullsUntilPity[rarity]
    }
    
    fun getProgress(rarity: SkinRarity, maxPulls: Int): Float {
        val counter = getCounter(rarity)
        return (counter.toFloat() / maxPulls.toFloat()).coerceIn(0f, 1f)
    }
    
    fun getProgressPercentage(rarity: SkinRarity, maxPulls: Int): Int {
        return (getProgress(rarity, maxPulls) * 100).toInt()
    }
    
    fun getTimeSinceLastUpdate(): Long {
        return System.currentTimeMillis() - lastUpdateTime
    }
    
    fun getTimeSinceLastUpdateMinutes(): Float {
        return getTimeSinceLastUpdate() / 60000f
    }
}

@Serializable
data class PityEvent(
    val id: String,
    val userId: String,
    val poolId: String,
    val rarity: SkinRarity?,
    val eventType: PityEventType,
    val countersBefore: Map<SkinRarity, Int>,
    val timestamp: Long
) {
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceEvent(): Long {
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceEventMinutes(): Float {
        return getTimeSinceEvent() / 60000f
    }
}

@Serializable
data class PityStats(
    val totalPityEvents: Int = 0,
    val rarityEvents: Map<SkinRarity, Int> = emptyMap(),
    val resetEvents: Int = 0
) {
    fun getRarityEventCount(rarity: SkinRarity): Int {
        return rarityEvents[rarity] ?: 0
    }
    
    fun getResetRate(): Float {
        return if (totalPityEvents > 0) {
            (resetEvents.toFloat() / totalPityEvents.toFloat()) * 100f
        } else {
            0f
        }
    }
}

@Serializable
data class PitySummary(
    val userId: String,
    val poolId: String,
    val totalPulls: Int,
    val rareCounter: Int,
    val superRareCounter: Int,
    val ultraRareCounter: Int,
    val rareProgress: Float,
    val superRareProgress: Float,
    val ultraRareProgress: Float,
    val isPityTriggered: Boolean,
    val triggeredRarity: SkinRarity?,
    val lastUpdateTime: Long
) {
    fun getRareProgressPercentage(): Int {
        return (rareProgress * 100).toInt()
    }
    
    fun getSuperRareProgressPercentage(): Int {
        return (superRareProgress * 100).toInt()
    }
    
    fun getUltraRareProgressPercentage(): Int {
        return (ultraRareProgress * 100).toInt()
    }
    
    fun getClosestPityRarity(): SkinRarity? {
        return listOf(
            SkinRarity.R to rareProgress,
            SkinRarity.SR to superRareProgress,
            SkinRarity.SSR to ultraRareProgress
        ).maxByOrNull { it.second }?.first
    }
}

@Serializable
data class PityExportData(
    val userId: String,
    val pityCounters: Map<String, Map<SkinRarity, Int>>,
    val pityStates: Map<String, PityState>,
    val pityHistory: List<PityEvent>,
    val exportTime: Long
) {
    fun getFormattedExportTime(): String {
        val date = java.util.Date(exportTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

enum class PityEventType {
    INCREMENT,
    RESET,
    MANUAL_RESET,
    SPECIFIC_RESET
}
