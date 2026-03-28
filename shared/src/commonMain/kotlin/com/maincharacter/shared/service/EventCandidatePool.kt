package com.maincharacter.shared.service

import com.maincharacter.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class EventCandidatePool(
    private val eventRegistry: EventRegistry,
    private val cooldownManager: EventCooldownManager
) {
    
    private val _candidatePool = MutableStateFlow<List<EventCandidate>>(emptyList())
    val candidatePool: StateFlow<List<EventCandidate>> = _candidatePool
    
    private val _poolStats = MutableStateFlow(PoolStats())
    val poolStats: StateFlow<PoolStats> = _poolStats
    
    fun refreshPool(
        hostState: HostState,
        userSkills: List<String>,
        userItems: List<String>
    ): PoolRefreshResult {
        val allEvents = eventRegistry.getAllEvents()
        
        val candidates = allEvents.mapNotNull { event ->
            val isAvailable = checkEventAvailability(
                event,
                hostState,
                userSkills,
                userItems
            )
            
            if (isAvailable) {
                val weight = calculateEventWeight(event, hostState, userSkills)
                
                EventCandidate(
                    eventId = event.id,
                    event = event,
                    weight = weight,
                    baseProbability = event.trigger.probability,
                    filteredReasons = emptyList()
                )
            } else {
                null
            }
        }
        
        _candidatePool.value = candidates
        
        val stats = calculatePoolStats(candidates)
        _poolStats.value = stats
        
        return PoolRefreshResult(
            success = true,
            totalEvents = allEvents.size,
            availableEvents = candidates.size,
            filteredEvents = allEvents.size - candidates.size,
            poolSize = candidates.size
        )
    }
    
    private fun checkEventAvailability(
        event: EventDef,
        hostState: HostState,
        userSkills: List<String>,
        userItems: List<String>
    ): Boolean {
        val reasons = mutableListOf<String>()
        
        if (!event.meetsRequirements(hostState)) {
            reasons.add("Requirements not met")
        }
        
        if (!cooldownManager.isEventAvailable(event.id)) {
            reasons.add("Event on cooldown")
        }
        
        if (event.requirements.requiredSkills.isNotEmpty()) {
            val hasRequiredSkills = event.requirements.requiredSkills.all { skillId ->
                userSkills.contains(skillId)
            }
            if (!hasRequiredSkills) {
                reasons.add("Missing required skills")
            }
        }
        
        if (event.requirements.requiredItems.isNotEmpty()) {
            val hasRequiredItems = event.requirements.requiredItems.all { itemId ->
                userItems.contains(itemId)
            }
            if (!hasRequiredItems) {
                reasons.add("Missing required items")
            }
        }
        
        if (event.requirements.forbiddenTags.isNotEmpty()) {
            val hasForbiddenTags = event.requirements.forbiddenTags.any { tag ->
                event.tags.contains(tag)
            }
            if (hasForbiddenTags) {
                reasons.add("Contains forbidden tags")
            }
        }
        
        val lastTriggerTime = cooldownManager.getLastTriggerTime(event.id)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastTrigger = currentTime - lastTriggerTime
        
        if (timeSinceLastTrigger < event.cooldown.minCooldownMs) {
            reasons.add("Minimum cooldown not met")
        }
        
        if (event.trigger.maxOccurrences > 0) {
            val occurrenceCount = cooldownManager.getOccurrenceCount(event.id)
            if (occurrenceCount >= event.trigger.maxOccurrences) {
                reasons.add("Max occurrences reached")
            }
        }
        
        if (event.trigger.timeWindow > 0) {
            val windowOccurrences = cooldownManager.getOccurrencesInWindow(
                event.id,
                event.trigger.timeWindow
            )
            if (windowOccurrences >= event.trigger.maxOccurrences) {
                reasons.add("Window limit reached")
            }
        }
        
        return reasons.isEmpty()
    }
    
    private fun calculateEventWeight(
        event: EventDef,
        hostState: HostState,
        userSkills: List<String>
    ): Float {
        var weight = event.trigger.probability
        
        weight *= getRarityMultiplier(event.rarity)
        
        weight *= getFortuneModifier(event, hostState.fortune)
        
        weight *= getBondModifier(event, hostState.bond)
        
        weight *= getSkillModifier(event, userSkills)
        
        weight *= getMoodModifier(event, hostState.mood)
        
        weight *= getVitalityModifier(event, hostState.vitality)
        
        return weight.coerceIn(0f, 1f)
    }
    
    private fun getRarityMultiplier(rarity: EventRarity): Float {
        return when (rarity) {
            EventRarity.COMMON -> 1.0f
            EventRarity.UNCOMMON -> 0.8f
            EventRarity.RARE -> 0.6f
            EventRarity.EPIC -> 0.4f
            EventRarity.LEGENDARY -> 0.2f
        }
    }
    
    private fun getFortuneModifier(event: EventDef, fortune: Int): Float {
        val fortuneRatio = fortune / 100f
        
        return when {
            fortuneRatio >= 0.8f -> 1.3f
            fortuneRatio >= 0.6f -> 1.1f
            fortuneRatio >= 0.4f -> 1.0f
            fortuneRatio >= 0.2f -> 0.9f
            else -> 0.8f
        }
    }
    
    private fun getBondModifier(event: EventDef, bond: Int): Float {
        val bondRatio = bond / 100f
        
        return when {
            bondRatio >= 0.8f -> 1.2f
            bondRatio >= 0.6f -> 1.1f
            bondRatio >= 0.4f -> 1.0f
            bondRatio >= 0.2f -> 0.9f
            else -> 0.8f
        }
    }
    
    private fun getSkillModifier(event: EventDef, userSkills: List<String>): Float {
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
    
    private fun getMoodModifier(event: EventDef, mood: Int): Float {
        val moodRatio = mood / 100f
        
        return when {
            moodRatio >= 0.8f -> 1.1f
            moodRatio >= 0.6f -> 1.05f
            moodRatio >= 0.4f -> 1.0f
            moodRatio >= 0.2f -> 0.95f
            else -> 0.9f
        }
    }
    
    private fun getVitalityModifier(event: EventDef, vitality: Int): Float {
        val vitalityRatio = vitality / 100f
        
        return when {
            vitalityRatio >= 0.8f -> 1.05f
            vitalityRatio >= 0.6f -> 1.0f
            vitalityRatio >= 0.4f -> 0.95f
            vitalityRatio >= 0.2f -> 0.9f
            else -> 0.85f
        }
    }
    
    fun getCandidatesByTag(tag: String): List<EventCandidate> {
        return _candidatePool.value.filter { candidate ->
            candidate.event.hasTag(tag)
        }
    }
    
    fun getCandidatesByType(type: EventType): List<EventCandidate> {
        return _candidatePool.value.filter { candidate ->
            candidate.event.type == type
        }
    }
    
    fun getCandidatesByRarity(rarity: EventRarity): List<EventCandidate> {
        return _candidatePool.value.filter { candidate ->
            candidate.event.rarity == rarity
        }
    }
    
    fun getCandidatesByMinBond(minBond: Int): List<EventCandidate> {
        return _candidatePool.value.filter { candidate ->
            candidate.event.requirements.minBond <= minBond
        }
    }
    
    fun getTopCandidates(count: Int): List<EventCandidate> {
        return _candidatePool.value
            .sortedByDescending { it.weight }
            .take(count)
    }
    
    fun getRandomCandidate(): EventCandidate? {
        val candidates = _candidatePool.value
        
        if (candidates.isEmpty()) {
            return null
        }
        
        val totalWeight = candidates.sumOf { it.weight.toDouble() }
        var random = Math.random() * totalWeight
        
        for (candidate in candidates) {
            random -= candidate.weight
            if (random <= 0) {
                return candidate
            }
        }
        
        return candidates.last()
    }
    
    fun getFilteredCandidates(
        filters: EventFilters
    ): List<EventCandidate> {
        var candidates = _candidatePool.value
        
        if (filters.tags.isNotEmpty()) {
            candidates = candidates.filter { candidate ->
                filters.tags.any { tag -> candidate.event.hasTag(tag) }
            }
        }
        
        if (filters.types.isNotEmpty()) {
            candidates = candidates.filter { candidate ->
                filters.types.contains(candidate.event.type)
            }
        }
        
        if (filters.rarities.isNotEmpty()) {
            candidates = candidates.filter { candidate ->
                filters.rarities.contains(candidate.event.rarity)
            }
        }
        
        if (filters.minBond != null) {
            candidates = candidates.filter { candidate ->
                candidate.event.requirements.minBond >= filters.minBond
            }
        }
        
        if (filters.maxBond != null) {
            candidates = candidates.filter { candidate ->
                candidate.event.requirements.maxBond <= filters.maxBond
            }
        }
        
        if (filters.minWeight != null) {
            candidates = candidates.filter { candidate ->
                candidate.weight >= filters.minWeight
            }
        }
        
        return candidates
    }
    
    private fun calculatePoolStats(candidates: List<EventCandidate>): PoolStats {
        val byType = candidates
            .groupBy { it.event.type }
            .mapValues { it.value.size }
        
        val byRarity = candidates
            .groupBy { it.event.rarity }
            .mapValues { it.value.size }
        
        val byTag = candidates
            .flatMap { it.event.tags }
            .groupingBy { it }
            .eachCount()
        
        val averageWeight = candidates
            .map { it.weight }
            .average()
            .toFloat()
        
        return PoolStats(
            totalCandidates = candidates.size,
            byType = byType,
            byRarity = byRarity,
            byTag = byTag,
            averageWeight = averageWeight,
            minWeight = candidates.minByOrNull { it.weight }?.weight ?: 0f,
            maxWeight = candidates.maxByOrNull { it.weight }?.weight ?: 0f
        )
    }
    
    fun getPoolSummary(): PoolSummary {
        val candidates = _candidatePool.value
        val stats = _poolStats.value
        
        return PoolSummary(
            totalCandidates = candidates.size,
            averageWeight = stats.averageWeight,
            mostCommonType = stats.byType.maxByOrNull { it.value }?.key,
            mostCommonRarity = stats.byRarity.maxByOrNull { it.value }?.key,
            mostCommonTag = stats.byTag.maxByOrNull { it.value }?.key,
            highestWeightEvent = candidates.maxByOrNull { it.weight }?.eventId,
            lowestWeightEvent = candidates.minByOrNull { it.weight }?.eventId
        )
    }
    
    fun clearPool() {
        _candidatePool.value = emptyList()
        _poolStats.value = PoolStats()
    }
}

@Serializable
data class EventCandidate(
    val eventId: String,
    val event: EventDef,
    val weight: Float,
    val baseProbability: Float,
    val filteredReasons: List<String>
)

@Serializable
data class PoolStats(
    val totalCandidates: Int = 0,
    val byType: Map<EventType, Int> = emptyMap(),
    val byRarity: Map<EventRarity, Int> = emptyMap(),
    val byTag: Map<String, Int> = emptyMap(),
    val averageWeight: Float = 0f,
    val minWeight: Float = 0f,
    val maxWeight: Float = 0f
)

@Serializable
data class PoolRefreshResult(
    val success: Boolean,
    val totalEvents: Int,
    val availableEvents: Int,
    val filteredEvents: Int,
    val poolSize: Int
)

@Serializable
data class EventFilters(
    val tags: List<String> = emptyList(),
    val types: List<EventType> = emptyList(),
    val rarities: List<EventRarity> = emptyList(),
    val minBond: Int? = null,
    val maxBond: Int? = null,
    val minWeight: Float? = null,
    val maxWeight: Float? = null
)

@Serializable
data class PoolSummary(
    val totalCandidates: Int,
    val averageWeight: Float,
    val mostCommonType: EventType?,
    val mostCommonRarity: EventRarity?,
    val mostCommonTag: String?,
    val highestWeightEvent: String?,
    val lowestWeightEvent: String?
)

interface EventRegistry {
    fun getAllEvents(): List<EventDef>
    fun getEventById(id: String): EventDef?
    fun getEventsByType(type: EventType): List<EventDef>
    fun getEventsByTag(tag: String): List<EventDef>
    fun getEventsByRarity(rarity: EventRarity): List<EventDef>
}

interface EventCooldownManager {
    fun isEventAvailable(eventId: String): Boolean
    fun getLastTriggerTime(eventId: String): Long
    fun getOccurrenceCount(eventId: String): Int
    fun getOccurrencesInWindow(eventId: String, windowMs: Long): Int
    fun recordTrigger(eventId: String)
    fun resetCooldown(eventId: String)
}
