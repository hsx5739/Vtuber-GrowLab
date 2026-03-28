package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class GachaConfigValidator(
    private val poolConfig: GachaPoolConfig
) {
    
    private val _validationResults = MutableStateFlow<Map<String, GachaValidationResult>>(emptyMap())
    val validationResults: StateFlow<Map<String, GachaValidationResult>> = _validationResults
    
    private val _validationHistory = MutableStateFlow<List<GachaValidationHistoryEntry>>(emptyList())
    val validationHistory: StateFlow<List<GachaValidationHistoryEntry>> = _validationHistory
    
    private val _validationStats = MutableStateFlow(GachaValidationStats())
    val validationStats: StateFlow<GachaValidationStats> = _validationStats
    
    fun validatePool(poolId: String): GachaValidationResult {
        val pool = poolConfig.getPool(poolId)
        
        if (pool == null) {
            val result = GachaValidationResult(
                poolId = poolId,
                isValid = false,
                errors = listOf("Pool not found: $poolId"),
                warnings = emptyList(),
                timestamp = System.currentTimeMillis()
            )
            
            recordValidationResult(result)
            return result
        }
        
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        validatePoolBasicInfo(pool, errors, warnings)
        validatePoolCost(pool, errors, warnings)
        validatePoolRows(pool, errors, warnings)
        validatePoolPityRules(pool, errors, warnings)
        validatePoolDuplicateRules(pool, errors, warnings)
        validatePoolProbabilityDisplay(pool, errors, warnings)
        validatePoolSkins(pool, errors, warnings)
        validatePoolTimeRange(pool, errors, warnings)
        
        val isValid = errors.isEmpty()
        
        val result = GachaValidationResult(
            poolId = poolId,
            isValid = isValid,
            errors = errors,
            warnings = warnings,
            timestamp = System.currentTimeMillis()
        )
        
        recordValidationResult(result)
        
        return result
    }
    
    fun validateAllPools(): Map<String, GachaValidationResult> {
        val pools = poolConfig.getAllPools()
        val results = mutableMapOf<String, GachaValidationResult>()
        
        pools.forEach { (poolId, _) ->
            val result = validatePool(poolId)
            results[poolId] = result
        }
        
        return results
    }
    
    fun validatePoolConfig(configJson: String): GachaConfigValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            val loadResult = poolConfig.loadConfig(configJson)
            
            if (!loadResult.success) {
                errors.add("Failed to load config: ${loadResult.error}")
                return GachaConfigValidationResult(
                    isValid = false,
                    errors = errors,
                    warnings = warnings,
                    poolResults = emptyMap(),
                    timestamp = System.currentTimeMillis()
                )
            }
            
            val poolResults = validateAllPools()
            
            poolResults.values.forEach { result ->
                if (!result.isValid) {
                    errors.addAll(result.errors)
                }
                warnings.addAll(result.warnings)
            }
            
            val isValid = errors.isEmpty()
            
            return GachaConfigValidationResult(
                isValid = isValid,
                errors = errors,
                warnings = warnings,
                poolResults = poolResults,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            errors.add("Exception during validation: ${e.message}")
            return GachaConfigValidationResult(
                isValid = false,
                errors = errors,
                warnings = warnings,
                poolResults = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    private fun validatePoolBasicInfo(pool: GachaPoolDef, errors: MutableList<String>, warnings: MutableList<String>) {
        if (pool.poolId.isBlank()) {
            errors.add("Pool ID cannot be blank")
        }
        
        if (pool.poolName.isBlank()) {
            errors.add("Pool name cannot be blank")
        }
        
        if (pool.poolDescription.isBlank()) {
            warnings.add("Pool description is blank")
        }
    }
    
    private fun validatePoolCost(pool: GachaPoolDef, errors: MutableList<String>, warnings: MutableList<String>) {
        val cost = pool.cost
        
        if (cost.singlePullCost <= 0) {
            errors.add("Single pull cost must be positive")
        }
        
        if (cost.tenPullCost <= 0) {
            errors.add("Ten pull cost must be positive")
        }
        
        if (cost.tenPullCost > cost.singlePullCost * 10) {
            errors.add("Ten pull cost exceeds 10x single pull cost")
        }
        
        if (cost.hasDiscount() && cost.getDiscountPercentage() > 50f) {
            warnings.add("Ten pull discount exceeds 50%")
        }
    }
    
    private fun validatePoolRows(pool: GachaPoolDef, errors: MutableList<String>, warnings: MutableList<String>) {
        if (pool.rows.isEmpty()) {
            errors.add("Pool must have at least one row")
            return
        }
        
        val rarities = mutableSetOf<SkinRarity>()
        
        pool.rows.forEach { row ->
            if (row.weight <= 0) {
                errors.add("Row ${row.rowId} has invalid weight: ${row.weight}")
            }
            
            if (row.skinIds.isEmpty()) {
                errors.add("Row ${row.rowId} has no skins")
            }
            
            if (row.rarity in rarities) {
                errors.add("Duplicate rarity ${row.rarity.name} in row ${row.rowId}")
            } else {
                rarities.add(row.rarity)
            }
            
            row.featuredSkinIds.forEach { featuredSkinId ->
                if (featuredSkinId !in row.skinIds) {
                    errors.add("Featured skin $featuredSkinId not found in row ${row.rowId}")
                }
            }
            
            if (row.guaranteedSkinId != null) {
                if (row.guaranteedSkinId !in row.skinIds) {
                    errors.add("Guaranteed skin ${row.guaranteedSkinId} not found in row ${row.rowId}")
                }
                
                if (row.minGuaranteedCount > row.maxGuaranteedCount) {
                    errors.add("Min guaranteed count exceeds max guaranteed count in row ${row.rowId}")
                }
            }
        }
        
        val totalWeight = pool.getTotalWeight()
        if (totalWeight <= 0) {
            errors.add("Total pool weight must be positive")
        }
    }
    
    private fun validatePoolPityRules(pool: GachaPoolDef, errors: MutableList<String>, warnings: MutableList<String>) {
        val pityRules = pool.pityRules
        
        if (pityRules.rarePullsMax <= 0) {
            errors.add("Rare pulls max must be positive")
        }
        
        if (pityRules.superRarePullsMax <= 0) {
            errors.add("Super rare pulls max must be positive")
        }
        
        if (pityRules.ultraRarePullsMax <= 0) {
            errors.add("Ultra rare pulls max must be positive")
        }
        
        if (pityRules.rarePullsMax >= pityRules.superRarePullsMax) {
            errors.add("Rare pulls max should be less than super rare pulls max")
        }
        
        if (pityRules.superRarePullsMax >= pityRules.ultraRarePullsMax) {
            errors.add("Super rare pulls max should be less than ultra rare pulls max")
        }
        
        if (pityRules.featuredSkinPity != null && pityRules.featuredSkinPity <= 0) {
            errors.add("Featured skin pity must be positive")
        }
        
        if (pityRules.featuredSkinPity != null && pityRules.featuredSkinPity > pityRules.ultraRarePullsMax) {
            warnings.add("Featured skin pity exceeds ultra rare pulls max")
        }
    }
    
    private fun validatePoolDuplicateRules(pool: GachaPoolDef, errors: MutableList<String>, warnings: MutableList<String>) {
        val duplicateRules = pool.duplicateRules
        
        if (duplicateRules.maxShardPerSkin <= 0) {
            errors.add("Max shard per skin must be positive")
        }
        
        if (duplicateRules.maxShardPerSkin > 9999) {
            warnings.add("Max shard per skin exceeds 9999")
        }
        
        duplicateRules.shardPerDuplicate.forEach { (rarity, shards) ->
            if (shards <= 0) {
                errors.add("Shard count for ${rarity.name} must be positive")
            }
            
            if (shards > duplicateRules.maxShardPerSkin) {
                errors.add("Shard count for ${rarity.name} exceeds max shard per skin")
            }
        }
        
        val expectedRarities = setOf(SkinRarity.N, SkinRarity.R, SkinRarity.SR, SkinRarity.SSR)
        val definedRarities = duplicateRules.shardPerDuplicate.keys
        
        val missingRarities = expectedRarities - definedRarities
        if (missingRarities.isNotEmpty()) {
            warnings.add("Missing shard counts for rarities: ${missingRarities.joinToString { it.name }}")
        }
    }
    
    private fun validatePoolProbabilityDisplay(pool: GachaPoolDef, errors: MutableList<String>, warnings: MutableList<String>) {
        val probabilityDisplay = pool.probabilityDisplay
        
        if (probabilityDisplay.displayProbabilities.isEmpty()) {
            errors.add("Display probabilities cannot be empty")
            return
        }
        
        var totalProbability = 0f
        probabilityDisplay.displayProbabilities.values.forEach { probability ->
            if (probability < 0f || probability > 1f) {
                errors.add("Invalid probability value: $probability (must be between 0 and 1)")
            }
            totalProbability += probability
        }
        
        if (totalProbability < 0.99f || totalProbability > 1.01f) {
            warnings.add("Total display probability ($totalProbability) is not close to 1.0")
        }
        
        if (probabilityDisplay.featuredProbability != null) {
            if (probabilityDisplay.featuredProbability < 0f || probabilityDisplay.featuredProbability > 1f) {
                errors.add("Featured probability must be between 0 and 1")
            }
        }
        
        if (probabilityDisplay.guaranteedProbability != null) {
            if (probabilityDisplay.guaranteedProbability < 0f || probabilityDisplay.guaranteedProbability > 1f) {
                errors.add("Guaranteed probability must be between 0 and 1")
            }
        }
    }
    
    private fun validatePoolSkins(pool: GachaPoolDef, errors: MutableList<String>, warnings: MutableList<String>) {
        val allSkinIds = mutableSetOf<String>()
        
        pool.rows.forEach { row ->
            row.skinIds.forEach { skinId ->
                if (skinId in allSkinIds) {
                    errors.add("Duplicate skin ID: $skinId")
                } else {
                    allSkinIds.add(skinId)
                }
            }
        }
        
        pool.featuredSkins.forEach { featuredSkinId ->
            if (featuredSkinId !in allSkinIds) {
                errors.add("Featured skin $featuredSkinId not found in any row")
            }
        }
        
        if (allSkinIds.isEmpty()) {
            errors.add("Pool must have at least one skin")
        }
    }
    
    private fun validatePoolTimeRange(pool: GachaPoolDef, errors: MutableList<String>, warnings: MutableList<String>) {
        if (pool.startTime <= 0) {
            errors.add("Start time must be positive")
        }
        
        if (pool.endTime <= 0) {
            errors.add("End time must be positive")
        }
        
        if (pool.startTime >= pool.endTime) {
            errors.add("Start time must be before end time")
        }
        
        val currentTime = System.currentTimeMillis()
        
        if (pool.startTime > currentTime) {
            warnings.add("Pool start time is in the future")
        }
        
        if (pool.endTime < currentTime) {
            warnings.add("Pool end time is in the past")
        }
        
        val poolDuration = pool.endTime - pool.startTime
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        if (poolDuration < oneDayMs) {
            warnings.add("Pool duration is less than 24 hours")
        }
        
        val oneYearMs = 365L * oneDayMs
        if (poolDuration > oneYearMs) {
            warnings.add("Pool duration exceeds 1 year")
        }
    }
    
    fun getValidationResult(poolId: String): GachaValidationResult? {
        return _validationResults.value[poolId]
    }
    
    fun getAllValidationResults(): Map<String, GachaValidationResult> {
        return _validationResults.value
    }
    
    fun getValidationHistory(poolId: String? = null): List<GachaValidationHistoryEntry> {
        var history = _validationHistory.value
        
        if (poolId != null) {
            history = history.filter { it.poolId == poolId }
        }
        
        return history
    }
    
    fun getRecentValidationHistory(limit: Int = 10): List<GachaValidationHistoryEntry> {
        return _validationHistory.value.takeLast(limit)
    }
    
    fun getValidationStats(): GachaValidationStats {
        return _validationStats.value
    }
    
    fun getValidationSummary(): GachaValidationSummary {
        val results = _validationResults.value
        val stats = _validationStats.value
        
        val totalPools = results.size
        val validPools = results.values.count { it.isValid }
        val invalidPools = totalPools - validPools
        
        val totalErrors = results.values.sumOf { it.errors.size }
        val totalWarnings = results.values.sumOf { it.warnings.size }
        
        val mostCommonErrors = mutableMapOf<String, Int>()
        results.values.forEach { result ->
            result.errors.forEach { error ->
                mostCommonErrors[error] = (mostCommonErrors[error] ?: 0) + 1
            }
        }
        
        val mostCommonWarnings = mutableMapOf<String, Int>()
        results.values.forEach { result ->
            result.warnings.forEach { warning ->
                mostCommonWarnings[warning] = (mostCommonWarnings[warning] ?: 0) + 1
            }
        }
        
        return GachaValidationSummary(
            totalPools = totalPools,
            validPools = validPools,
            invalidPools = invalidPools,
            totalErrors = totalErrors,
            totalWarnings = totalWarnings,
            mostCommonErrors = mostCommonErrors.entries.sortedByDescending { it.value }.take(10),
            mostCommonWarnings = mostCommonWarnings.entries.sortedByDescending { it.value }.take(10),
            lastValidationTime = stats.lastValidationTime
        )
    }
    
    fun clearValidationResults() {
        _validationResults.value = emptyMap()
    }
    
    fun clearValidationHistory() {
        _validationHistory.value = emptyList()
    }
    
    fun clearAll() {
        _validationResults.value = emptyMap()
        _validationHistory.value = emptyList()
        _validationStats.value = GachaValidationStats()
    }
    
    private fun recordValidationResult(result: GachaValidationResult) {
        val updatedResults = _validationResults.value.toMutableMap()
        updatedResults[result.poolId] = result
        _validationResults.value = updatedResults
        
        val entry = GachaValidationHistoryEntry(
            id = generateHistoryId(),
            poolId = result.poolId,
            isValid = result.isValid,
            errorCount = result.errors.size,
            warningCount = result.warnings.size,
            timestamp = result.timestamp
        )
        
        val updatedHistory = _validationHistory.value.toMutableList()
        updatedHistory.add(entry)
        _validationHistory.value = updatedHistory
        
        val current = _validationStats.value
        val updated = current.copy(
            totalValidations = current.totalValidations + 1,
            validPools = if (result.isValid) current.validPools + 1 else current.validPools,
            invalidPools = if (!result.isValid) current.invalidPools + 1 else current.invalidPools,
            totalErrors = current.totalErrors + result.errors.size,
            totalWarnings = current.totalWarnings + result.warnings.size,
            lastValidationTime = result.timestamp
        )
        _validationStats.value = updated
    }
    
    private fun generateHistoryId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "validation_history_${timestamp}_$random"
    }
}

@Serializable
data class GachaValidationResult(
    val poolId: String,
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>,
    val timestamp: Long
) {
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceValidation(): Long {
        return System.currentTimeMillis() - timestamp
    }
    
    fun getTimeSinceValidationMinutes(): Float {
        return getTimeSinceValidation() / 60000f
    }
    
    fun getErrorSummary(): String {
        return if (errors.isEmpty()) {
            "No errors"
        } else {
            "${errors.size} error(s): ${errors.joinToString("; ")}"
        }
    }
    
    fun getWarningSummary(): String {
        return if (warnings.isEmpty()) {
            "No warnings"
        } else {
            "${warnings.size} warning(s): ${warnings.joinToString("; ")}"
        }
    }
    
    fun getFullSummary(): String {
        return buildString {
            appendLine("Pool: $poolId")
            appendLine("Status: ${if (isValid) "Valid" else "Invalid"}")
            appendLine("Errors: ${errors.size}")
            appendLine("Warnings: ${warnings.size}")
            if (errors.isNotEmpty()) {
                appendLine("Error Details:")
                errors.forEach { appendLine("  - $it") }
            }
            if (warnings.isNotEmpty()) {
                appendLine("Warning Details:")
                warnings.forEach { appendLine("  - $it") }
            }
            appendLine("Validated: ${getFormattedTimestamp()}")
        }
    }
}

@Serializable
data class GachaConfigValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>,
    val poolResults: Map<String, GachaValidationResult>,
    val timestamp: Long
) {
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getValidPoolCount(): Int {
        return poolResults.values.count { it.isValid }
    }
    
    fun getInvalidPoolCount(): Int {
        return poolResults.values.count { !it.isValid }
    }
    
    fun getFullSummary(): String {
        return buildString {
            appendLine("=== Config Validation Result ===")
            appendLine("Overall Status: ${if (isValid) "Valid" else "Invalid"}")
            appendLine("Total Errors: ${errors.size}")
            appendLine("Total Warnings: ${warnings.size}")
            appendLine("Valid Pools: ${getValidPoolCount()}")
            appendLine("Invalid Pools: ${getInvalidPoolCount()}")
            appendLine()
            
            if (errors.isNotEmpty()) {
                appendLine("Global Errors:")
                errors.forEach { appendLine("  - $it") }
                appendLine()
            }
            
            if (warnings.isNotEmpty()) {
                appendLine("Global Warnings:")
                warnings.forEach { appendLine("  - $it") }
                appendLine()
            }
            
            appendLine("Pool Details:")
            poolResults.values.forEach { result ->
                appendLine("  Pool: ${result.poolId} - ${if (result.isValid) "Valid" else "Invalid"}")
                if (result.errors.isNotEmpty()) {
                    result.errors.forEach { appendLine("    Error: $it") }
                }
                if (result.warnings.isNotEmpty()) {
                    result.warnings.forEach { appendLine("    Warning: $it") }
                }
            }
            
            appendLine()
            appendLine("Validated: ${getFormattedTimestamp()}")
        }
    }
}

@Serializable
data class GachaValidationHistoryEntry(
    val id: String,
    val poolId: String,
    val isValid: Boolean,
    val errorCount: Int,
    val warningCount: Int,
    val timestamp: Long
) {
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getTimeSinceValidation(): Long {
        return System.currentTimeMillis() - timestamp
    }
}

@Serializable
data class GachaValidationStats(
    val totalValidations: Int = 0,
    val validPools: Int = 0,
    val invalidPools: Int = 0,
    val totalErrors: Int = 0,
    val totalWarnings: Int = 0,
    val lastValidationTime: Long = 0L
) {
    fun getValidationRate(): Float {
        return if (totalValidations > 0) {
            (validPools.toFloat() / totalValidations.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getAverageErrorsPerValidation(): Float {
        return if (totalValidations > 0) {
            totalErrors.toFloat() / totalValidations.toFloat()
        } else {
            0f
        }
    }
    
    fun getAverageWarningsPerValidation(): Float {
        return if (totalValidations > 0) {
            totalWarnings.toFloat() / totalValidations.toFloat()
        } else {
            0f
        }
    }
    
    fun getFormattedLastValidationTime(): String {
        val date = java.util.Date(lastValidationTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
data class GachaValidationSummary(
    val totalPools: Int,
    val validPools: Int,
    val invalidPools: Int,
    val totalErrors: Int,
    val totalWarnings: Int,
    val mostCommonErrors: List<Map.Entry<String, Int>>,
    val mostCommonWarnings: List<Map.Entry<String, Int>>,
    val lastValidationTime: Long
) {
    fun getValidationRate(): Float {
        return if (totalPools > 0) {
            (validPools.toFloat() / totalPools.toFloat()) * 100f
        } else {
            0f
        }
    }
    
    fun getAverageErrorsPerPool(): Float {
        return if (totalPools > 0) {
            totalErrors.toFloat() / totalPools.toFloat()
        } else {
            0f
        }
    }
    
    fun getAverageWarningsPerPool(): Float {
        return if (totalPools > 0) {
            totalWarnings.toFloat() / totalPools.toFloat()
        } else {
            0f
        }
    }
    
    fun getFormattedLastValidationTime(): String {
        val date = java.util.Date(lastValidationTime)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    fun getSummaryText(): String {
        return buildString {
            appendLine("=== Validation Summary ===")
            appendLine("Total Pools: $totalPools")
            appendLine("Valid: $validPools (${getValidationRate().toInt()}%)")
            appendLine("Invalid: $invalidPools")
            appendLine("Total Errors: $totalErrors")
            appendLine("Total Warnings: $totalWarnings")
            appendLine("Avg Errors/Pool: ${getAverageErrorsPerPool().toInt()}")
            appendLine("Avg Warnings/Pool: ${getAverageWarningsPerPool().toInt()}")
            appendLine()
            
            if (mostCommonErrors.isNotEmpty()) {
                appendLine("Most Common Errors:")
                mostCommonErrors.take(5).forEach { (error, count) ->
                    appendLine("  $error ($count occurrences)")
                }
                appendLine()
            }
            
            if (mostCommonWarnings.isNotEmpty()) {
                appendLine("Most Common Warnings:")
                mostCommonWarnings.take(5).forEach { (warning, count) ->
                    appendLine("  $warning ($count occurrences)")
                }
            }
            
            appendLine()
            appendLine("Last Validation: ${getFormattedLastValidationTime()}")
        }
    }
}
