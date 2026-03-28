package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class SkinConfigValidator(
    private val skinConfig: SkinConfig
) {
    
    private val _validationResults = MutableStateFlow<Map<String, SkinValidationResult>>(emptyMap())
    val validationResults: StateFlow<Map<String, SkinValidationResult>> = _validationResults
    
    private val _validationHistory = MutableStateFlow<List<ValidationHistoryEntry>>(emptyList())
    val validationHistory: StateFlow<List<ValidationHistoryEntry>> = _validationHistory
    
    private val _validationStats = MutableStateFlow(SkinValidationStats())
    val validationStats: StateFlow<SkinValidationStats> = _validationStats
    
    fun validateSkin(skinId: String): SkinValidationResult {
        val skin = skinConfig.getSkin(skinId)
        
        if (skin == null) {
            val result = SkinValidationResult(
                skinId = skinId,
                isValid = false,
                errors = listOf("Skin not found: $skinId"),
                warnings = emptyList(),
                timestamp = System.currentTimeMillis()
            )
            
            recordValidationResult(result)
            return result
        }
        
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        validateSkinBasicInfo(skin, errors, warnings)
        validateSkinRarity(skin, errors, warnings)
        validateSkinType(skin, errors, warnings)
        validateSkinImages(skin, errors, warnings)
        validateSkinUnlockSources(skin, errors, warnings)
        validateSkinGachaPools(skin, errors, warnings)
        validateSkinShardCost(skin, errors, warnings)
        validateSkinBondBonus(skin, errors, warnings)
        validateSkinAttributes(skin, errors, warnings)
        validateSkinAvailability(skin, errors, warnings)
        
        val isValid = errors.isEmpty()
        
        val result = SkinValidationResult(
            skinId = skinId,
            isValid = isValid,
            errors = errors,
            warnings = warnings,
            timestamp = System.currentTimeMillis()
        )
        
        recordValidationResult(result)
        
        return result
    }
    
    fun validateAllSkins(): Map<String, SkinValidationResult> {
        val skins = skinConfig.getAllSkins()
        val results = mutableMapOf<String, SkinValidationResult>()
        
        skins.forEach { (skinId, _) ->
            results[skinId] = validateSkin(skinId)
        }
        
        return results
    }
    
    fun validateSkinConfig(): SkinValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        validateConfigVersion(errors, warnings)
        validateDefaultSkin(errors, warnings)
        validateSkinIds(errors, warnings)
        validateDuplicateSkins(errors, warnings)
        validateSkinReferences(errors, warnings)
        validateSkinAvailabilityConsistency(errors, warnings)
        
        val isValid = errors.isEmpty()
        
        val result = SkinValidationResult(
            skinId = "config",
            isValid = isValid,
            errors = errors,
            warnings = warnings,
            timestamp = System.currentTimeMillis()
        )
        
        recordValidationResult(result)
        
        return result
    }
    
    private fun validateSkinBasicInfo(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (skin.skinId.isEmpty()) {
            errors.add("Skin ID is empty")
        }
        
        if (skin.skinName.isEmpty()) {
            errors.add("Skin name is empty")
        }
        
        if (skin.skinDescription.isEmpty()) {
            warnings.add("Skin description is empty")
        }
        
        if (skin.skinName.length > 100) {
            warnings.add("Skin name exceeds 100 characters")
        }
        
        if (skin.skinDescription.length > 500) {
            warnings.add("Skin description exceeds 500 characters")
        }
    }
    
    private fun validateSkinRarity(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (!SkinRarity.values().contains(skin.rarity)) {
            errors.add("Invalid rarity: ${skin.rarity}")
        }
    }
    
    private fun validateSkinType(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (!SkinType.values().contains(skin.skinType)) {
            errors.add("Invalid skin type: ${skin.skinType}")
        }
        
        if (skin.skinType == SkinType.DEFAULT && skin.baseSkinId != null) {
            warnings.add("Default skin should not have a base skin ID")
        }
        
        if (skin.skinType != SkinType.DEFAULT && skin.baseSkinId == null) {
            warnings.add("Non-default skin should have a base skin ID")
        }
    }
    
    private fun validateSkinImages(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (skin.iconPath.isEmpty()) {
            errors.add("Icon path is empty")
        }
        
        if (skin.previewImagePath.isEmpty()) {
            errors.add("Preview image path is empty")
        }
        
        if (skin.fullImagePath.isEmpty()) {
            errors.add("Full image path is empty")
        }
        
        if (!skin.iconPath.endsWith(".png") && !skin.iconPath.endsWith(".jpg") && !skin.iconPath.endsWith(".webp")) {
            warnings.add("Icon path should end with .png, .jpg, or .webp")
        }
        
        if (!skin.previewImagePath.endsWith(".png") && !skin.previewImagePath.endsWith(".jpg") && !skin.previewImagePath.endsWith(".webp")) {
            warnings.add("Preview image path should end with .png, .jpg, or .webp")
        }
        
        if (!skin.fullImagePath.endsWith(".png") && !skin.fullImagePath.endsWith(".jpg") && !skin.fullImagePath.endsWith(".webp")) {
            warnings.add("Full image path should end with .png, .jpg, or .webp")
        }
    }
    
    private fun validateSkinUnlockSources(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (skin.unlockSources.isEmpty()) {
            errors.add("No unlock sources defined")
        }
        
        val invalidSources = skin.unlockSources.filter { !SkinUnlockSource.values().contains(it) }
        if (invalidSources.isNotEmpty()) {
            errors.add("Invalid unlock sources: $invalidSources")
        }
        
        if (skin.unlockSources.contains(SkinUnlockSource.DEFAULT) && skin.skinType != SkinType.DEFAULT) {
            warnings.add("Non-default skin should not have DEFAULT unlock source")
        }
    }
    
    private fun validateSkinGachaPools(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (skin.unlockSources.contains(SkinUnlockSource.GACHA)) {
            if (skin.gachaPoolIds.isEmpty()) {
                errors.add("Gacha unlock source defined but no gacha pool IDs provided")
            }
            
            val invalidPoolIds = skin.gachaPoolIds.filter { it.isEmpty() }
            if (invalidPoolIds.isNotEmpty()) {
                errors.add("Empty gacha pool IDs: $invalidPoolIds")
            }
        } else if (skin.gachaPoolIds.isNotEmpty()) {
            warnings.add("Gacha pool IDs provided but GACHA not in unlock sources")
        }
    }
    
    private fun validateSkinShardCost(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (skin.unlockSources.contains(SkinUnlockSource.SHARD_SYNTHESIS)) {
            if (skin.shardCost <= 0) {
                errors.add("Shard synthesis unlock source defined but shard cost is invalid: ${skin.shardCost}")
            }
            
            if (skin.shardCost > 999) {
                errors.add("Shard cost exceeds maximum of 999: ${skin.shardCost}")
            }
        } else if (skin.shardCost > 0) {
            warnings.add("Shard cost provided but SHARD_SYNTHESIS not in unlock sources")
        }
    }
    
    private fun validateSkinBondBonus(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (skin.bondBonus.baseBonus < 0) {
            errors.add("Bond base bonus cannot be negative: ${skin.bondBonus.baseBonus}")
        }
        
        if (skin.bondBonus.bonusPerLevel < 0) {
            errors.add("Bond bonus per level cannot be negative: ${skin.bondBonus.bonusPerLevel}")
        }
        
        if (skin.bondBonus.maxLevel <= 0) {
            errors.add("Bond max level must be positive: ${skin.bondBonus.maxLevel}")
        }
        
        if (skin.bondBonus.maxLevel > 100) {
            warnings.add("Bond max level exceeds 100: ${skin.bondBonus.maxLevel}")
        }
        
        if (skin.bondBonus.affectedAttributes.isEmpty()) {
            warnings.add("No affected attributes defined for bond bonus")
        }
        
        val invalidAttributes = skin.bondBonus.affectedAttributes.filter { !AttributeType.values().contains(it) }
        if (invalidAttributes.isNotEmpty()) {
            errors.add("Invalid affected attributes: $invalidAttributes")
        }
    }
    
    private fun validateSkinAttributes(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        if (skin.skinAttributes.moodBonus < 0) {
            errors.add("Mood bonus cannot be negative: ${skin.skinAttributes.moodBonus}")
        }
        
        if (skin.skinAttributes.vitalityBonus < 0) {
            errors.add("Vitality bonus cannot be negative: ${skin.skinAttributes.vitalityBonus}")
        }
        
        if (skin.skinAttributes.fortuneBonus < 0) {
            errors.add("Fortune bonus cannot be negative: ${skin.skinAttributes.fortuneBonus}")
        }
        
        if (skin.skinAttributes.focusBonus < 0) {
            errors.add("Focus bonus cannot be negative: ${skin.skinAttributes.focusBonus}")
        }
        
        if (skin.skinAttributes.bondBonus < 0) {
            errors.add("Bond bonus cannot be negative: ${skin.skinAttributes.bondBonus}")
        }
        
        val totalBonus = skin.skinAttributes.getTotalBonus()
        if (totalBonus > 100) {
            warnings.add("Total attribute bonus exceeds 100: $totalBonus")
        }
    }
    
    private fun validateSkinAvailability(skin: SkinDefinition, errors: MutableList<String>, warnings: MutableList<String>) {
        val availability = skin.availability
        
        if (availability.startTime != null && availability.endTime != null) {
            if (availability.startTime >= availability.endTime) {
                errors.add("Start time must be before end time")
            }
        }
        
        if (availability.requiredLevel != null && availability.requiredLevel < 0) {
            errors.add("Required level cannot be negative: ${availability.requiredLevel}")
        }
        
        if (availability.requiredLevel != null && availability.requiredLevel > 100) {
            warnings.add("Required level exceeds 100: ${availability.requiredLevel}")
        }
        
        if (!availability.isAvailable && availability.startTime == null && availability.endTime == null) {
            warnings.add("Skin marked as unavailable but no time range specified")
        }
    }
    
    private fun validateConfigVersion(errors: MutableList<String>, warnings: MutableList<String>) {
        val version = skinConfig.getConfigVersionValue()
        
        if (version.isEmpty()) {
            errors.add("Config version is empty")
        }
        
        if (!version.matches(Regex("\\d+\\.\\d+\\.\\d+"))) {
            warnings.add("Config version should follow semantic versioning (e.g., 1.0.0)")
        }
    }
    
    private fun validateDefaultSkin(errors: MutableList<String>, warnings: MutableList<String>) {
        val defaultSkinId = skinConfig.defaultSkinId
        
        if (defaultSkinId.isEmpty()) {
            errors.add("Default skin ID is empty")
        } else {
            val defaultSkin = skinConfig.getSkin(defaultSkinId)
            if (defaultSkin == null) {
                errors.add("Default skin not found: $defaultSkinId")
            } else if (defaultSkin.skinType != SkinType.DEFAULT) {
                errors.add("Default skin must have type DEFAULT")
            }
        }
    }
    
    private fun validateSkinIds(errors: MutableList<String>, warnings: MutableList<String>) {
        val skins = skinConfig.getAllSkins()
        
        val emptyIds = skins.keys.filter { it.isEmpty() }
        if (emptyIds.isNotEmpty()) {
            errors.add("Empty skin IDs: $emptyIds")
        }
        
        val invalidIds = skins.keys.filter { !it.matches(Regex("^[a-zA-Z0-9_-]+$")) }
        if (invalidIds.isNotEmpty()) {
            warnings.add("Skin IDs contain invalid characters: $invalidIds")
        }
    }
    
    private fun validateDuplicateSkins(errors: MutableList<String>, warnings: MutableList<String>) {
        val skins = skinConfig.getAllSkins()
        val ids = skins.keys.toList()
        
        val duplicates = ids.groupBy { it }.filter { it.value.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            errors.add("Duplicate skin IDs: $duplicates")
        }
        
        val names = skins.values.map { it.skinName }
        val duplicateNames = names.groupBy { it }.filter { it.value.size > 1 }.keys
        if (duplicateNames.isNotEmpty()) {
            warnings.add("Duplicate skin names: $duplicateNames")
        }
    }
    
    private fun validateSkinReferences(errors: MutableList<String>, warnings: MutableList<String>) {
        val skins = skinConfig.getAllSkins()
        val allSkinIds = skins.keys.toSet()
        
        skins.forEach { (skinId, skin) ->
            if (skin.baseSkinId != null && skin.baseSkinId !in allSkinIds) {
                errors.add("Base skin not found: ${skin.baseSkinId} (referenced by $skinId)")
            }
        }
    }
    
    private fun validateSkinAvailabilityConsistency(errors: MutableList<String>, warnings: MutableList<String>) {
        val skins = skinConfig.getAllSkins()
        val currentTime = System.currentTimeMillis()
        
        val expiredSkins = skins.values.filter { 
            it.availability.endTime != null && it.availability.endTime < currentTime 
        }
        
        if (expiredSkins.isNotEmpty()) {
            warnings.add("Expired skins found: ${expiredSkins.map { it.skinId }}")
        }
        
        val notYetAvailableSkins = skins.values.filter { 
            it.availability.startTime != null && it.availability.startTime > currentTime 
        }
        
        if (notYetAvailableSkins.isNotEmpty()) {
            warnings.add("Not yet available skins found: ${notYetAvailableSkins.map { it.skinId }}")
        }
    }
    
    private fun recordValidationResult(result: SkinValidationResult) {
        val updatedResults = _validationResults.value.toMutableMap()
        updatedResults[result.skinId] = result
        _validationResults.value = updatedResults
        
        val historyEntry = ValidationHistoryEntry(
            id = generateHistoryId(),
            skinId = result.skinId,
            isValid = result.isValid,
            errorCount = result.errors.size,
            warningCount = result.warnings.size,
            timestamp = result.timestamp
        )
        
        val updatedHistory = _validationHistory.value.toMutableList()
        updatedHistory.add(historyEntry)
        _validationHistory.value = updatedHistory
        
        updateStats(result)
    }
    
    private fun updateStats(result: SkinValidationResult) {
        val current = _validationStats.value
        
        val updated = current.copy(
            totalValidations = current.totalValidations + 1,
            validSkins = if (result.isValid) current.validSkins + 1 else current.validSkins,
            invalidSkins = if (!result.isValid) current.invalidSkins + 1 else current.invalidSkins,
            totalErrors = current.totalErrors + result.errors.size,
            totalWarnings = current.totalWarnings + result.warnings.size,
            lastValidationTime = result.timestamp
        )
        
        _validationStats.value = updated
    }
    
    fun getValidationResult(skinId: String): SkinValidationResult? {
        return _validationResults.value[skinId]
    }
    
    fun getAllValidationResults(): Map<String, SkinValidationResult> {
        return _validationResults.value
    }
    
    fun getInvalidSkins(): List<String> {
        return _validationResults.value
            .filter { !it.value.isValid }
            .keys
            .toList()
    }
    
    fun getSkinsWithWarnings(): List<String> {
        return _validationResults.value
            .filter { it.value.warnings.isNotEmpty() }
            .keys
            .toList()
    }
    
    fun getValidationHistory(skinId: String): List<ValidationHistoryEntry> {
        return _validationHistory.value.filter { it.skinId == skinId }
    }
    
    fun getRecentHistory(limit: Int = 10): List<ValidationHistoryEntry> {
        return _validationHistory.value.takeLast(limit)
    }
    
    fun getValidationStats(): SkinValidationStats {
        return _validationStats.value
    }
    
    fun exportValidationReport(): String {
        val stats = _validationStats.value
        val results = _validationResults.value
        
        return buildString {
            appendLine("=== Skin Configuration Validation Report ===")
            appendLine("Config Version: ${skinConfig.getConfigVersionValue()}")
            appendLine("Total Skins: ${skinConfig.getSkinCount()}")
            appendLine("Report Time: ${java.util.Date()}")
            appendLine()
            
            appendLine("--- Statistics ---")
            appendLine("Total Validations: ${stats.totalValidations}")
            appendLine("Valid Skins: ${stats.validSkins}")
            appendLine("Invalid Skins: ${stats.invalidSkins}")
            appendLine("Total Errors: ${stats.totalErrors}")
            appendLine("Total Warnings: ${stats.totalWarnings}")
            appendLine("Success Rate: ${String.format("%.2f", stats.getSuccessRate())}%")
            appendLine("Last Validation: ${getFormattedLastValidationTime()}")
            appendLine()
            
            appendLine("--- Invalid Skins ---")
            val invalidSkins = getInvalidSkins()
            if (invalidSkins.isEmpty()) {
                appendLine("None")
            } else {
                invalidSkins.forEach { skinId ->
                    val result = results[skinId]
                    appendLine("Skin ID: $skinId")
                    result?.errors?.forEach { error ->
                        appendLine("  Error: $error")
                    }
                }
            }
            appendLine()
            
            appendLine("--- Skins with Warnings ---")
            val skinsWithWarnings = getSkinsWithWarnings()
            if (skinsWithWarnings.isEmpty()) {
                appendLine("None")
            } else {
                skinsWithWarnings.forEach { skinId ->
                    val result = results[skinId]
                    appendLine("Skin ID: $skinId")
                    result?.warnings?.forEach { warning ->
                        appendLine("  Warning: $warning")
                    }
                }
            }
        }
    }
    
    fun clearValidationData() {
        _validationResults.value = emptyMap()
        _validationHistory.value = emptyList()
        _validationStats.value = SkinValidationStats()
    }
    
    private fun generateHistoryId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "skin_validation_history_${timestamp}_$random"
    }
    
    private fun getFormattedLastValidationTime(): String {
        val timestamp = _validationStats.value.lastValidationTime
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}

@Serializable
data class SkinValidationResult(
    val skinId: String,
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
            "${errors.size} error(s): ${errors.joinToString(", ")}"
        }
    }
    
    fun getWarningSummary(): String {
        return if (warnings.isEmpty()) {
            "No warnings"
        } else {
            "${warnings.size} warning(s): ${warnings.joinToString(", ")}"
        }
    }
}

@Serializable
data class ValidationHistoryEntry(
    val id: String,
    val skinId: String,
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
    
    fun getTimeSinceValidationMinutes(): Float {
        return getTimeSinceValidation() / 60000f
    }
}

@Serializable
data class SkinValidationStats(
    val totalValidations: Int = 0,
    val validSkins: Int = 0,
    val invalidSkins: Int = 0,
    val totalErrors: Int = 0,
    val totalWarnings: Int = 0,
    val lastValidationTime: Long = 0L
) {
    fun getSuccessRate(): Float {
        return if (totalValidations > 0) {
            (validSkins.toFloat() / totalValidations.toFloat()) * 100f
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
    
    fun getTimeSinceLastValidation(): Long {
        return System.currentTimeMillis() - lastValidationTime
    }
    
    fun getTimeSinceLastValidationMinutes(): Float {
        return getTimeSinceLastValidation() / 60000f
    }
}

