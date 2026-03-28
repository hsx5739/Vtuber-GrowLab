package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class GachaHistoryManager {
    
    private val _pullHistory = MutableStateFlow<List<GachaHistory>>(emptyList())
    val pullHistory: StateFlow<List<GachaHistory>> = _pullHistory
    
    private val _historyStats = MutableStateFlow(HistoryStats())
    val historyStats: StateFlow<HistoryStats> = _historyStats
    
    private val _auditLog = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLog: StateFlow<List<AuditLogEntry>> = _auditLog
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    fun recordPull(history: GachaHistory): RecordResult {
        try {
            val updatedHistory = _pullHistory.value.toMutableList()
            updatedHistory.add(history)
            _pullHistory.value = updatedHistory
            
            recordAuditLog(
                userId = history.userId,
                action = AuditAction.PULL,
                poolId = history.poolId,
                pullId = history.pullId,
                details = mapOf(
                    "pullType" to history.pullType.name,
                    "pullCount" to history.pullCount.toString(),
                    "totalCost" to history.totalCost.toString(),
                    "currencyType" to history.cost.currencyType.name,
                    "results" to history.results.size.toString()
                )
            )
            
            updateStats(history)
            
            return RecordResult(
                success = true,
                error = null,
                historyId = history.pullId
            )
        } catch (e: Exception) {
            return RecordResult(
                success = false,
                error = "Failed to record pull: ${e.message}",
                historyId = null
            )
        }
    }
    
    fun recordBatchPull(histories: List<GachaHistory>): BatchRecordResult {
        val results = mutableListOf<RecordResult>()
        var successCount = 0
        var failureCount = 0
        
        histories.forEach { history ->
            val result = recordPull(history)
            results.add(result)
            
            if (result.success) {
                successCount++
            } else {
                failureCount++
            }
        }
        
        return BatchRecordResult(
            success = successCount > 0,
            error = if (failureCount > 0) "Some records failed" else null,
            results = results,
            successCount = successCount,
            failureCount = failureCount
        )
    }
    
    fun getPullHistory(
        userId: String? = null,
        poolId: String? = null,
        pullType: PullType? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        limit: Int = 100
    ): List<GachaHistory> {
        var history = _pullHistory.value
        
        if (userId != null) {
            history = history.filter { it.userId == userId }
        }
        
        if (poolId != null) {
            history = history.filter { it.poolId == poolId }
        }
        
        if (pullType != null) {
            history = history.filter { it.pullType == pullType }
        }
        
        if (startTime != null) {
            history = history.filter { it.timestamp >= startTime }
        }
        
        if (endTime != null) {
            history = history.filter { it.timestamp <= endTime }
        }
        
        return history.takeLast(limit)
    }
    
    fun getUserPullHistory(userId: String, limit: Int = 100): List<GachaHistory> {
        return getPullHistory(userId = userId, limit = limit)
    }
    
    fun getPoolPullHistory(poolId: String, limit: Int = 100): List<GachaHistory> {
        return getPullHistory(poolId = poolId, limit = limit)
    }
    
    fun getPullHistoryById(pullId: String): GachaHistory? {
        return _pullHistory.value.find { it.pullId == pullId }
    }
    
    fun getSessionHistory(sessionId: String): List<GachaHistory> {
        return _pullHistory.value.filter { it.sessionId == sessionId }
    }
    
    fun getRecentPulls(limit: Int = 10): List<GachaHistory> {
        return _pullHistory.value.takeLast(limit)
    }
    
    fun getPullHistoryStats(
        userId: String? = null,
        poolId: String? = null,
        startTime: Long? = null,
        endTime: Long? = null
    ): PullHistoryStats {
        val history = getPullHistory(
            userId = userId,
            poolId = poolId,
            startTime = startTime,
            endTime = endTime,
            limit = Int.MAX_VALUE
        )
        
        val totalPulls = history.size
        val totalCost = history.sumOf { it.totalCost }
        
        val singlePulls = history.count { it.pullType == PullType.SINGLE }
        val tenPulls = history.count { it.pullType == PullType.TEN }
        
        val totalResults = history.sumOf { it.results.size }
        
        val rarityStats = mutableMapOf<SkinRarity, Int>()
        history.forEach { h ->
            h.results.forEach { result ->
                rarityStats[result.rarity] = (rarityStats[result.rarity] ?: 0) + 1
            }
        }
        
        val ssrCount = rarityStats[SkinRarity.SSR] ?: 0
        val srCount = rarityStats[SkinRarity.SR] ?: 0
        val rCount = rarityStats[SkinRarity.R] ?: 0
        val nCount = rarityStats[SkinRarity.N] ?: 0
        
        val newSkins = mutableSetOf<String>()
        val duplicateSkins = mutableSetOf<String>()
        var totalShards = 0
        
        history.forEach { h ->
            h.results.forEach { result ->
                if (result.isNew) {
                    newSkins.add(result.skinId)
                } else {
                    duplicateSkins.add(result.skinId)
                    totalShards += result.getShardReward()
                }
            }
        }
        
        val pityTriggeredCount = history.count { h ->
            h.results.any { it.hasPityTriggered() }
        }
        
        val averageCostPerPull = if (totalPulls > 0) {
            totalCost.toFloat() / totalPulls.toFloat()
        } else {
            0f
        }
        
        val ssrRate = if (totalResults > 0) {
            (ssrCount.toFloat() / totalResults.toFloat()) * 100f
        } else {
            0f
        }
        
        val srRate = if (totalResults > 0) {
            (srCount.toFloat() / totalResults.toFloat()) * 100f
        } else {
            0f
        }
        
        val rRate = if (totalResults > 0) {
            (rCount.toFloat() / totalResults.toFloat()) * 100f
        } else {
            0f
        }
        
        val nRate = if (totalResults > 0) {
            (nCount.toFloat() / totalResults.toFloat()) * 100f
        } else {
            0f
        }
        
        return PullHistoryStats(
            totalPulls = totalPulls,
            totalCost = totalCost,
            averageCostPerPull = averageCostPerPull,
            singlePulls = singlePulls,
            tenPulls = tenPulls,
            totalResults = totalResults,
            ssrCount = ssrCount,
            srCount = srCount,
            rCount = rCount,
            nCount = nCount,
            ssrRate = ssrRate,
            srRate = srRate,
            rRate = rRate,
            nRate = nRate,
            newSkins = newSkins.size,
            duplicateSkins = duplicateSkins.size,
            totalShards = totalShards,
            pityTriggeredCount = pityTriggeredCount
        )
    }
    
    fun getUserPullSummary(userId: String): UserPullSummary {
        val history = getUserPullHistory(userId, Int.MAX_VALUE)
        
        val stats = getPullHistoryStats(userId = userId)
        
        val poolStats = mutableMapOf<String, PoolPullStats>()
        
        history.forEach { h ->
            val poolId = h.poolId
            val poolStat = poolStats.getOrPut(poolId) {
                PoolPullStats(
                    poolId = poolId,
                    pullCount = 0,
                    totalCost = 0,
                    totalResults = 0,
                    ssrCount = 0,
                    srCount = 0,
                    rCount = 0,
                    nCount = 0
                )
            }
            
            poolStats[poolId] = poolStat.copy(
                pullCount = poolStat.pullCount + 1,
                totalCost = poolStat.totalCost + h.totalCost,
                totalResults = poolStat.totalResults + h.results.size,
                ssrCount = poolStat.ssrCount + h.results.count { it.rarity == SkinRarity.SSR },
                srCount = poolStat.srCount + h.results.count { it.rarity == SkinRarity.SR },
                rCount = poolStat.rCount + h.results.count { it.rarity == SkinRarity.R },
                nCount = poolStat.nCount + h.results.count { it.rarity == SkinRarity.N }
            )
        }
        
        val mostUsedPool = poolStats.maxByOrNull { it.value.pullCount }?.key
        val mostExpensivePool = poolStats.maxByOrNull { it.value.totalCost }?.key
        val luckiestPool = poolStats.maxByOrNull { 
            if (it.value.totalResults > 0) {
                (it.value.ssrCount.toFloat() / it.value.totalResults.toFloat())
            } else {
                0f
            }
        }?.key
        
        val firstPullTime = history.minByOrNull { it.timestamp }?.timestamp
        val lastPullTime = history.maxByOrNull { it.timestamp }?.timestamp
        
        return UserPullSummary(
            userId = userId,
            stats = stats,
            poolStats = poolStats,
            mostUsedPool = mostUsedPool,
            mostExpensivePool = mostExpensivePool,
            luckiestPool = luckiestPool,
            firstPullTime = firstPullTime,
            lastPullTime = lastPullTime,
            totalPlayTime = if (firstPullTime != null && lastPullTime != null) {
                lastPullTime - firstPullTime
            } else {
                0L
            }
        )
    }
    
    fun recordAuditLog(
        userId: String,
        action: AuditAction,
        poolId: String? = null,
        pullId: String? = null,
        details: Map<String, String> = emptyMap()
    ) {
        val entry = AuditLogEntry(
            id = generateAuditLogId(),
            userId = userId,
            action = action,
            poolId = poolId,
            pullId = pullId,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        
        val updatedLog = _auditLog.value.toMutableList()
        updatedLog.add(entry)
        _auditLog.value = updatedLog
    }
    
    fun getAuditLog(
        userId: String? = null,
        action: AuditAction? = null,
        poolId: String? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        limit: Int = 100
    ): List<AuditLogEntry> {
        var log = _auditLog.value
        
        if (userId != null) {
            log = log.filter { it.userId == userId }
        }
        
        if (action != null) {
            log = log.filter { it.action == action }
        }
        
        if (poolId != null) {
            log = log.filter { it.poolId == poolId }
        }
        
        if (startTime != null) {
            log = log.filter { it.timestamp >= startTime }
        }
        
        if (endTime != null) {
            log = log.filter { it.timestamp <= endTime }
        }
        
        return log.takeLast(limit)
    }
    
    fun getUserAuditLog(userId: String, limit: Int = 100): List<AuditLogEntry> {
        return getAuditLog(userId = userId, limit = limit)
    }
    
    fun getPullAuditLog(pullId: String): List<AuditLogEntry> {
        return _auditLog.value.filter { it.pullId == pullId }
    }
    
    fun getRecentAuditLog(limit: Int = 10): List<AuditLogEntry> {
        return _auditLog.value.takeLast(limit)
    }
    
    fun exportPullHistory(
        userId: String? = null,
        poolId: String? = null,
        startTime: Long? = null,
        endTime: Long? = null
    ): String {
        val history = getPullHistory(
            userId = userId,
            poolId = poolId,
            startTime = startTime,
            endTime = endTime,
            limit = Int.MAX_VALUE
        )
        
        val exportData = PullHistoryExport(
            userId = userId,
            poolId = poolId,
            startTime = startTime,
            endTime = endTime,
            history = history,
            stats = getPullHistoryStats(userId, poolId, startTime, endTime),
            exportTime = System.currentTimeMillis()
        )
        
        return json.encodeToString(exportData)
    }
    
    fun exportAuditLog(
        userId: String? = null,
        poolId: String? = null,
        startTime: Long? = null,
        endTime: Long? = null
    ): String {
        val log = getAuditLog(
            userId = userId,
            poolId = poolId,
            startTime = startTime,
            endTime = endTime,
            limit = Int.MAX_VALUE
        )
        
        val exportData = AuditLogExport(
            userId = userId,
            poolId = poolId,
            startTime = startTime,
            endTime = endTime,
            auditLog = log,
            exportTime = System.currentTimeMillis()
        )
        
        return json.encodeToString(exportData)
    }
    
    fun clearPullHistory(userId: String) {
        val updatedHistory = _pullHistory.value.filter { it.userId != userId }
        _pullHistory.value = updatedHistory
    }
    
    fun clearAuditLog(userId: String) {
        val updatedLog = _auditLog.value.filter { it.userId != userId }
        _auditLog.value = updatedLog
    }
    
    fun clearAllHistory() {
        _pullHistory.value = emptyList()
        _auditLog.value = emptyList()
        _historyStats.value = HistoryStats()
    }
    
    fun getHistoryStats(): HistoryStats {
        return _historyStats.value
    }
    
    private fun updateStats(history: GachaHistory) {
        val current = _historyStats.value
        
        val updated = current.copy(
            totalPulls = current.totalPulls + 1,
            totalResults = current.totalResults + history.results.size,
            totalCost = current.totalCost + history.totalCost,
            userPulls = current.userPulls.toMutableMap().apply {
                this[history.userId] = (this[history.userId] ?: 0) + 1
            },
            poolPulls = current.poolPulls.toMutableMap().apply {
                this[history.poolId] = (this[history.poolId] ?: 0) + 1
            }
        )
        
        _historyStats.value = updated
    }
    
    private fun generateAuditLogId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "audit_${timestamp}_$random"
    }
}

@Serializable
data class RecordResult(
    val success: Boolean,
    val error: String?,
    val historyId: String?
) {
    fun isSuccessful(): Boolean {
        return success
    }
    
    fun getErrorMessage(): String? {
        return error
    }
    
    fun getRecordedHistoryId(): String? {
        return historyId
    }
}

@Serializable
data class BatchRecordResult(
    val success: Boolean,
    val error: String?,
    val results: List<RecordResult>,
    val successCount: Int,
    val failureCount: Int
) {
    fun isSuccessful(): Boolean {
        return success
    }
    
    fun getErrorMessage(): String? {
        return error
    }
    
    fun getSuccessRate(): Float {
        val total = successCount + failureCount
        return if (total > 0) {
            (successCount.toFloat() / total.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getFailureRate(): Float {
        val total = successCount + failureCount
        return if (total > 0) {
            (failureCount.toFloat() / total.toFloat()) * 100f
        } else {
            0f
        }
    }
}

@Serializable
data class PullHistoryStats(
    val totalPulls: Int,
    val totalCost: Int,
    val averageCostPerPull: Float,
    val singlePulls: Int,
    val tenPulls: Int,
    val totalResults: Int,
    val ssrCount: Int,
    val srCount: Int,
    val rCount: Int,
    val nCount: Int,
    val ssrRate: Float,
    val srRate: Float,
    val rRate: Float,
    val nRate: Float,
    val newSkins: Int,
    val duplicateSkins: Int,
    val totalShards: Int,
    val pityTriggeredCount: Int
) {
    fun getSSRPercentage(): Int {
        return ssrRate.toInt()
    }
    
    fun getSRPercentage(): Int {
        return srRate.toInt()
    }
    
    fun getRPercentage(): Int {
        return rRate.toInt()
    }
    
    fun getNPercentage(): Int {
        return nRate.toInt()
    }
    
    fun getNewSkinRate(): Float {
        return if (totalResults > 0) {
            (newSkins.toFloat() / totalResults.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getDuplicateSkinRate(): Float {
        return if (totalResults > 0) {
            (duplicateSkins.toFloat() / totalResults.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getPityTriggerRate(): Float {
        return if (totalPulls > 0) {
            (pityTriggeredCount.toFloat() / totalPulls.toFloat()) * 100f
        } else {
            0f
        }
    }
}

@Serializable
data class PoolPullStats(
    val poolId: String,
    val pullCount: Int,
    val totalCost: Int,
    val totalResults: Int,
    val ssrCount: Int,
    val srCount: Int,
    val rCount: Int,
    val nCount: Int
) {
    fun getAverageCostPerPull(): Float {
        return if (pullCount > 0) {
            totalCost.toFloat() / pullCount.toFloat()
        } else {
            0f
        }
    }
    
    fun getSSRRate(): Float {
        return if (totalResults > 0) {
            (ssrCount.toFloat() / totalResults.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getSRRate(): Float {
        return if (totalResults > 0) {
            (srCount.toFloat() / totalResults.toFloat()) * 100f
        } else {
            0f
        }
    }
}

@Serializable
data class UserPullSummary(
    val userId: String,
    val stats: PullHistoryStats,
    val poolStats: Map<String, PoolPullStats>,
    val mostUsedPool: String?,
    val mostExpensivePool: String?,
    val luckiestPool: String?,
    val firstPullTime: Long?,
    val lastPullTime: Long?,
    val totalPlayTime: Long
) {
    fun getPlayTimeDays(): Float {
        return totalPlayTime / (1000f * 60f * 60f * 24f)
    }
    
    fun getPlayTimeHours(): Float {
        return totalPlayTime / (1000f * 60f * 60f)
    }
    
    fun getFormattedFirstPullTime(): String? {
        val timestamp = firstPullTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getFormattedLastPullTime(): String? {
        val timestamp = lastPullTime ?: return null
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
data class AuditLogEntry(
    val id: String,
    val userId: String,
    val action: AuditAction,
    val poolId: String?,
    val pullId: String?,
    val details: Map<String, String>,
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
data class HistoryStats(
    val totalPulls: Int = 0,
    val totalResults: Int = 0,
    val totalCost: Int = 0,
    val userPulls: Map<String, Int> = emptyMap(),
    val poolPulls: Map<String, Int> = emptyMap()
) {
    fun getAverageResultsPerPull(): Float {
        return if (totalPulls > 0) {
            totalResults.toFloat() / totalPulls.toFloat()
        } else {
            0f
        }
    }
    
    fun getAverageCostPerPull(): Float {
        return if (totalPulls > 0) {
            totalCost.toFloat() / totalPulls.toFloat()
        } else {
            0f
        }
    }
    
    fun getMostActiveUser(): String? {
        return userPulls.maxByOrNull { it.value }?.key
    }
    
    fun getMostActivePool(): String? {
        return poolPulls.maxByOrNull { it.value }?.key
    }
}

@Serializable
data class PullHistoryExport(
    val userId: String?,
    val poolId: String?,
    val startTime: Long?,
    val endTime: Long?,
    val history: List<GachaHistory>,
    val stats: PullHistoryStats,
    val exportTime: Long
) {
    fun getFormattedExportTime(): String {
        val date = java.util.Date(exportTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
data class AuditLogExport(
    val userId: String?,
    val poolId: String?,
    val startTime: Long?,
    val endTime: Long?,
    val auditLog: List<AuditLogEntry>,
    val exportTime: Long
) {
    fun getFormattedExportTime(): String {
        val date = java.util.Date(exportTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

enum class AuditAction {
    PULL,
    CURRENCY_DEDUCT,
    SKIN_ADD,
    SHARD_ADD,
    PITY_RESET,
    CONFIG_CHANGE,
    USER_ACTION
}
