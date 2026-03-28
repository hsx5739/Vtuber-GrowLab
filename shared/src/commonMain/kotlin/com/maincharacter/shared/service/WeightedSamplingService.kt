package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlinx.serialization.Serializable

class WeightedSamplingService(
    private val eventRegistry: EventRegistry,
    private val skillRegistry: SkillRegistry
) {
    
    private val _samplingHistory = MutableStateFlow<List<SamplingRecord>>(emptyList())
    val samplingHistory: StateFlow<List<SamplingRecord>> = _samplingHistory
    
    private val _samplingStats = MutableStateFlow(SamplingStats())
    val samplingStats: StateFlow<SamplingStats> = _samplingStats
    
    fun sampleEvent(
        candidates: List<EventCandidate>,
        hostState: HostState,
        userSkills: List<String>,
        config: SamplingConfig = SamplingConfig()
    ): SamplingResult {
        if (candidates.isEmpty()) {
            return SamplingResult(
                success = false,
                selectedEvent = null,
                reason = "No candidates available",
                samplingDetails = null
            )
        }
        
        val weightedCandidates = applyWeightModifiers(
            candidates,
            hostState,
            userSkills,
            config
        )
        
        val selected = performWeightedSampling(
            weightedCandidates,
            config
        )
        
        val record = SamplingRecord(
            id = generateRecordId(),
            timestamp = System.currentTimeMillis(),
            totalCandidates = candidates.size,
            selectedEventId = selected?.eventId,
            hostState = hostState,
            userSkills = userSkills,
            samplingDetails = SamplingDetails(
                candidates = weightedCandidates,
                totalWeight = weightedCandidates.sumOf { it.modifiedWeight.toDouble() },
                samplingMethod = config.samplingMethod,
                fortuneModifier = getFortuneModifier(hostState.fortune),
                skillModifiers = getSkillModifiers(userSkills, weightedCandidates)
            )
        )
        
        _samplingHistory.value = _samplingHistory.value + record
        updateStats(record)
        
        return SamplingResult(
            success = selected != null,
            selectedEvent = selected,
            reason = if (selected != null) "Event selected" else "Sampling failed",
            samplingDetails = record.samplingDetails
        )
    }
    
    private fun applyWeightModifiers(
        candidates: List<EventCandidate>,
        hostState: HostState,
        userSkills: List<String>,
        config: SamplingConfig
    ): List<WeightedCandidate> {
        return candidates.map { candidate ->
            var weight = candidate.weight
            
            weight *= applyFortuneModifier(
                weight,
                hostState.fortune,
                candidate.event,
                config
            )
            
            weight *= applySkillModifiers(
                weight,
                candidate.event,
                userSkills,
                config
            )
            
            weight *= applyRarityModifier(
                weight,
                candidate.event.rarity,
                config
            )
            
            weight *= applyTagModifier(
                weight,
                candidate.event.tags,
                config
            )
            
            weight = clampWeight(weight, config)
            
            WeightedCandidate(
                eventId = candidate.eventId,
                event = candidate.event,
                originalWeight = candidate.weight,
                modifiedWeight = weight,
                modifiers = calculateModifiers(
                    candidate,
                    hostState,
                    userSkills,
                    config
                )
            )
        }
    }
    
    private fun applyFortuneModifier(
        weight: Float,
        fortune: Int,
        event: EventDef,
        config: SamplingConfig
    ): Float {
        if (!config.enableFortuneModifier) {
            return 1.0f
        }
        
        val fortuneRatio = fortune / 100f
        val baseModifier = when {
            fortuneRatio >= 0.9f -> 1.5f
            fortuneRatio >= 0.8f -> 1.3f
            fortuneRatio >= 0.6f -> 1.1f
            fortuneRatio >= 0.4f -> 1.0f
            fortuneRatio >= 0.2f -> 0.9f
            else -> 0.8f
        }
        
        val variance = (Random.nextFloat() - 0.5f) * config.fortuneVariance
        
        return baseModifier + variance
    }
    
    private fun applySkillModifiers(
        weight: Float,
        event: EventDef,
        userSkills: List<String>,
        config: SamplingConfig
    ): Float {
        if (!config.enableSkillModifier || event.skillInteractions.isEmpty()) {
            return 1.0f
        }
        
        var modifier = 1.0f
        
        event.skillInteractions.forEach { interaction ->
            if (userSkills.contains(interaction.skillId)) {
                when (interaction.interactionType) {
                    SkillInteractionType.WEIGHT_MODIFIER -> {
                        modifier *= interaction.modifier
                    }
                    SkillInteractionType.PROBABILITY_BOOST -> {
                        modifier *= interaction.modifier
                    }
                    else -> {}
                }
            }
        }
        
        return modifier
    }
    
    private fun applyRarityModifier(
        weight: Float,
        rarity: EventRarity,
        config: SamplingConfig
    ): Float {
        if (!config.enableRarityModifier) {
            return 1.0f
        }
        
        val modifier = when (rarity) {
            EventRarity.COMMON -> config.commonRarityModifier
            EventRarity.UNCOMMON -> config.uncommonRarityModifier
            EventRarity.RARE -> config.rareRarityModifier
            EventRarity.EPIC -> config.epicRarityModifier
            EventRarity.LEGENDARY -> config.legendaryRarityModifier
        }
        
        return modifier
    }
    
    private fun applyTagModifier(
        weight: Float,
        tags: List<String>,
        config: SamplingConfig
    ): Float {
        if (!config.enableTagModifier || config.tagModifiers.isEmpty()) {
            return 1.0f
        }
        
        var modifier = 1.0f
        
        tags.forEach { tag ->
            val tagModifier = config.tagModifiers[tag]
            if (tagModifier != null) {
                modifier *= tagModifier
            }
        }
        
        return modifier
    }
    
    private fun clampWeight(weight: Float, config: SamplingConfig): Float {
        return weight.coerceIn(config.minWeight, config.maxWeight)
    }
    
    private fun calculateModifiers(
        candidate: EventCandidate,
        hostState: HostState,
        userSkills: List<String>,
        config: SamplingConfig
    ): WeightModifiers {
        val fortuneMod = applyFortuneModifier(
            1.0f,
            hostState.fortune,
            candidate.event,
            config
        )
        
        val skillMod = applySkillModifiers(
            1.0f,
            candidate.event,
            userSkills,
            config
        )
        
        val rarityMod = applyRarityModifier(
            1.0f,
            candidate.event.rarity,
            config
        )
        
        val tagMod = applyTagModifier(
            1.0f,
            candidate.event.tags,
            config
        )
        
        return WeightModifiers(
            fortuneModifier = fortuneMod,
            skillModifier = skillMod,
            rarityModifier = rarityMod,
            tagModifier = tagMod,
            totalModifier = fortuneMod * skillMod * rarityMod * tagMod
        )
    }
    
    private fun performWeightedSampling(
        candidates: List<WeightedCandidate>,
        config: SamplingConfig
    ): WeightedCandidate? {
        if (candidates.isEmpty()) {
            return null
        }
        
        return when (config.samplingMethod) {
            SamplingMethod.WEIGHTED_RANDOM -> weightedRandomSampling(candidates)
            SamplingMethod.TIERED_SAMPLING -> tieredSampling(candidates, config)
            SamplingMethod.SOFTMAX -> softmaxSampling(candidates, config)
            SamplingMethod.UNIFORM -> uniformSampling(candidates)
        }
    }
    
    private fun weightedRandomSampling(
        candidates: List<WeightedCandidate>
    ): WeightedCandidate? {
        val totalWeight = candidates.sumOf { it.modifiedWeight.toDouble() }
        
        if (totalWeight <= 0) {
            return candidates.random()
        }
        
        var random = Random.nextDouble() * totalWeight
        
        for (candidate in candidates) {
            random -= candidate.modifiedWeight
            if (random <= 0) {
                return candidate
            }
        }
        
        return candidates.last()
    }
    
    private fun tieredSampling(
        candidates: List<WeightedCandidate>,
        config: SamplingConfig
    ): WeightedCandidate? {
        val tiers = candidates.groupBy { candidate ->
            when (candidate.event.rarity) {
                EventRarity.LEGENDARY -> 5
                EventRarity.EPIC -> 4
                EventRarity.RARE -> 3
                EventRarity.UNCOMMON -> 2
                EventRarity.COMMON -> 1
            }
        }
        
        val tierProbabilities = mapOf(
            5 to config.legendaryProbability,
            4 to config.epicProbability,
            3 to config.rareProbability,
            2 to config.uncommonProbability,
            1 to config.commonProbability
        )
        
        val random = Random.nextDouble()
        var cumulative = 0.0
        
        for ((tier, probability) in tierProbabilities) {
            cumulative += probability
            if (random <= cumulative) {
                val tierCandidates = tiers[tier] ?: emptyList()
                return if (tierCandidates.isNotEmpty()) {
                    weightedRandomSampling(tierCandidates)
                } else {
                    continue
                }
            }
        }
        
        return weightedRandomSampling(candidates)
    }
    
    private fun softmaxSampling(
        candidates: List<WeightedCandidate>,
        config: SamplingConfig
    ): WeightedCandidate? {
        val temperature = config.softmaxTemperature
        
        val expWeights = candidates.map { candidate ->
            Math.exp((candidate.modifiedWeight / temperature).toDouble())
        }
        
        val sumExp = expWeights.sum()
        
        val probabilities = expWeights.map { it / sumExp }
        
        var random = Random.nextDouble()
        var cumulative = 0.0
        
        for (i in candidates.indices) {
            cumulative += probabilities[i]
            if (random <= cumulative) {
                return candidates[i]
            }
        }
        
        return candidates.last()
    }
    
    private fun uniformSampling(
        candidates: List<WeightedCandidate>
    ): WeightedCandidate? {
        return candidates.random()
    }
    
    private fun getFortuneModifier(fortune: Int): Float {
        val fortuneRatio = fortune / 100f
        return when {
            fortuneRatio >= 0.9f -> 1.5f
            fortuneRatio >= 0.8f -> 1.3f
            fortuneRatio >= 0.6f -> 1.1f
            fortuneRatio >= 0.4f -> 1.0f
            fortuneRatio >= 0.2f -> 0.9f
            else -> 0.8f
        }
    }
    
    private fun getSkillModifiers(
        userSkills: List<String>,
        candidates: List<WeightedCandidate>
    ): Map<String, Float> {
        val modifiers = mutableMapOf<String, Float>()
        
        candidates.forEach { candidate ->
            candidate.event.skillInteractions.forEach { interaction ->
                if (userSkills.contains(interaction.skillId)) {
                    val current = modifiers[interaction.skillId] ?: 1.0f
                    modifiers[interaction.skillId] = current * interaction.modifier
                }
            }
        }
        
        return modifiers
    }
    
    private fun updateStats(record: SamplingRecord) {
        val current = _samplingStats.value
        
        val updated = current.copy(
            totalSamplings = current.totalSamplings + 1,
            eventSelections = current.eventSelections.toMutableMap().apply {
                val eventId = record.samplingDetails?.candidates?.find { 
                    it.eventId == record.selectedEventId 
                }?.eventId
                if (eventId != null) {
                    this[eventId] = (this[eventId] ?: 0) + 1
                }
            },
            averageWeight = if (current.totalSamplings > 0) {
                val newAvg = (current.averageWeight * current.totalSamplings +
                    (record.samplingDetails?.totalWeight?.toFloat() ?: 0f)) /
                    (current.totalSamplings + 1)
                newAvg
            } else {
                record.samplingDetails?.totalWeight?.toFloat() ?: 0f
            }
        )
        
        _samplingStats.value = updated
    }
    
    fun getSamplingHistory(limit: Int = 100): List<SamplingRecord> {
        return _samplingHistory.value.takeLast(limit)
    }
    
    fun getEventSelectionStats(eventId: String): EventSelectionStats? {
        val history = _samplingHistory.value
        val eventRecords = history.filter { 
            it.samplingDetails?.candidates?.any { it.eventId == eventId } == true 
        }
        
        if (eventRecords.isEmpty()) {
            return null
        }
        
        val selectedCount = eventRecords.count { it.selectedEventId == eventId }
        val totalCount = eventRecords.size
        
        return EventSelectionStats(
            eventId = eventId,
            totalAppearances = totalCount,
            selectedCount = selectedCount,
            selectionRate = if (totalCount > 0) {
                (selectedCount.toFloat() / totalCount.toFloat()) * 100f
            } else {
                0f
            },
            averageWeight = eventRecords.mapNotNull { record ->
                record.samplingDetails?.candidates?.find { it.eventId == eventId }?.modifiedWeight
            }.average().toFloat()
        )
    }
    
    fun clearHistory() {
        _samplingHistory.value = emptyList()
        _samplingStats.value = SamplingStats()
    }
    
    private fun generateRecordId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "sampling_${timestamp}_$random"
    }
}

@Serializable
data class WeightedCandidate(
    val eventId: String,
    val event: EventDef,
    val originalWeight: Float,
    val modifiedWeight: Float,
    val modifiers: WeightModifiers
)

@Serializable
data class WeightModifiers(
    val fortuneModifier: Float,
    val skillModifier: Float,
    val rarityModifier: Float,
    val tagModifier: Float,
    val totalModifier: Float
)

@Serializable
data class SamplingConfig(
    val samplingMethod: SamplingMethod = SamplingMethod.WEIGHTED_RANDOM,
    val enableFortuneModifier: Boolean = true,
    val enableSkillModifier: Boolean = true,
    val enableRarityModifier: Boolean = true,
    val enableTagModifier: Boolean = true,
    val fortuneVariance: Float = 0.1f,
    val minWeight: Float = 0.01f,
    val maxWeight: Float = 10.0f,
    val commonRarityModifier: Float = 1.0f,
    val uncommonRarityModifier: Float = 1.0f,
    val rareRarityModifier: Float = 1.0f,
    val epicRarityModifier: Float = 1.0f,
    val legendaryRarityModifier: Float = 1.0f,
    val tagModifiers: Map<String, Float> = emptyMap(),
    val commonProbability: Double = 0.6,
    val uncommonProbability: Double = 0.25,
    val rareProbability: Double = 0.1,
    val epicProbability: Double = 0.04,
    val legendaryProbability: Double = 0.01,
    val softmaxTemperature: Float = 1.0f
)

enum class SamplingMethod {
    WEIGHTED_RANDOM,
    TIERED_SAMPLING,
    SOFTMAX,
    UNIFORM
}

@Serializable
data class SamplingResult(
    val success: Boolean,
    val selectedEvent: WeightedCandidate?,
    val reason: String,
    val samplingDetails: SamplingDetails?
)

@Serializable
data class SamplingDetails(
    val candidates: List<WeightedCandidate>,
    val totalWeight: Double,
    val samplingMethod: SamplingMethod,
    val fortuneModifier: Float,
    val skillModifiers: Map<String, Float>
)

@Serializable
data class SamplingRecord(
    val id: String,
    val timestamp: Long,
    val totalCandidates: Int,
    val selectedEventId: String?,
    val hostState: HostState,
    val userSkills: List<String>,
    val samplingDetails: SamplingDetails?
)

@Serializable
data class SamplingStats(
    val totalSamplings: Int = 0,
    val eventSelections: Map<String, Int> = emptyMap(),
    val averageWeight: Float = 0f
)

@Serializable
data class EventSelectionStats(
    val eventId: String,
    val totalAppearances: Int,
    val selectedCount: Int,
    val selectionRate: Float,
    val averageWeight: Float
)

interface SkillRegistry {
    fun getSkillById(id: String): SkillDef?
    fun getAllSkills(): List<SkillDef>
    fun getSkillsByType(type: SkillType): List<SkillDef>
}
